/* Copyright 2016 Palo Alto Networks Inc.
 * All Rights Reserved.
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package com.paloaltonetworks.panorama.api.methods;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.mapping.CommitResponse;
import com.paloaltonetworks.panorama.api.mapping.GetAddressResponse;
import com.paloaltonetworks.panorama.api.mapping.GetTagResponse;
import com.paloaltonetworks.panorama.api.mapping.PANResponse;
import com.paloaltonetworks.panorama.api.mapping.ShowResponse;
import com.paloaltonetworks.panorama.api.mapping.TagEntry;
import com.paloaltonetworks.panorama.api.mapping.VMAuthKeyResponse;
import com.paloaltonetworks.utils.QuickXmlizerUtil;

public class PanoramaApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(PanoramaApiClient.class);

    // Public constants shared by many

    // Values for type parameter
    public static final String OP_TYPE = "op";
    public static final String CONFIG_TYPE = "config";

    public static final String XPATH_DEVGROUP_PREFIX = "/config/devices/entry[ @name=\"localhost.localdomain\" ]/device-group";
    public static final String XPATH_DEVGROUP_TEMPL = XPATH_DEVGROUP_PREFIX + "/entry[ @name=\"%s\" ]";

    // Values for action parameter
    public static final String SET_ACTION = "set";
    public static final String GET_ACTION = "get";
    public static final String DELETE_ACTION = "delete";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private static final String PAN_REST_URL_BASE = "/api/";

    private static final String KEYGEN_TYPE = "keygen";
    private static final String COMMIT_TYPE = "commit";

    // Http request paramenter names
    private static final String ELEMENT = "element";
    private static final String XPATH = "xpath";
    private static final String KEY = "key";
    private static final String TYPE = "type";
    private static final String ACTION = "action";
    private static final String CMD = "cmd";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    // Values for xpath parameter

    private static final String XPATH_ADDRESS_TEMPL = XPATH_DEVGROUP_TEMPL + "/address";
    private static final String XPATH_ADDRESS_TAGMEMBER_TEMPL = XPATH_ADDRESS_TEMPL + "/entry[ @name=\"%s\" ]/tag"
                                                            + "/member[text()='%s']";

    private static final String GENKEY_CMD_TEMPL = "<request><bootstrap><vm-auth-key><generate><lifetime> %s"
            + "</lifetime></generate></vm-auth-key></bootstrap></request>";

    private static final String COMMIT_CMD = "<commit></commit>";

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String FAILURE = "failure";

    private final String host;
    private final int port;
    private final Client client;
    private final String scheme;
    private String apiKey;

    public PanoramaApiClient(String panServer, int port, boolean isHttps, String loginName, String password, Client client)
            throws Exception {
        this.host = panServer;
        this.port = port <= 0 ? isHttps ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT : port;
        this.scheme = isHttps ? HTTPS : HTTP;
        this.client = client;
        this.apiKey = login(loginName, password);
    }

    public boolean checkConnection() {
        boolean status = false;

        if (getApiKey() != null) {
            status = true;
        }
        return status;
    }

    public String getVMAuthKey(String days) throws Exception {

        LOG.info("Generating VM Auth Key for " + days + " days");
        String cmd = String.format(GENKEY_CMD_TEMPL, days);
        Map<String, String> queryStrings = makeOpCmdRequestParams(cmd);
        VMAuthKeyResponse vmAuthKeyResponse = getRequest(queryStrings, VMAuthKeyResponse.class);
        String vmAuthKeyString = vmAuthKeyResponse.getShowResult();
        if (vmAuthKeyString == null) {
            throw new NullPointerException("Null result generating vmAuth with params: " + queryStrings);
        }

        String[] temp = vmAuthKeyString.split(" ");

        if (temp.length < 4) {
            throw new Exception("Error parsing vmAuth Response: it contains no vmAuth key ");
        }

        return temp[3];
    }

    public String configCommit() throws Exception {
        String status = FAILURE;

        Map<String, String> queryStrings = makeRequestParams(null, COMMIT_TYPE, null, null, COMMIT_CMD);

        try {
            CommitResponse commitResponse = getRequest(queryStrings, CommitResponse.class);
            status = commitResponse.getStatus();
            LOG.info("Commit Status : {} and Commit Code : {}", status, commitResponse.getCode());
        } catch (Exception e) {
            LOG.error("Commit failed", e);
        }

        return status;
    }

    public void configCommitOrThrow(String errorMessage) throws Exception {
        String configStatus = configCommit();
        if (configStatus.equals(ERROR)) {
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }
    }

    public List<AddressEntry> fetchAddressesWithTag(String sgTag, String devGroup) throws Exception {
        return getAddressEntries(devGroup).stream()
                .filter(a -> a.getTagNames() != null && a.getTagNames().contains(sgTag))
                .collect(toList());
    }

    public void addTagToAddress(String ip, String tag, String devGroup) throws Exception {
        LOG.info("Adding tag {} to address {}", tag, ip);
        String xpath = String.format(XPATH_ADDRESS_TEMPL, devGroup);
        AddressEntry address = new AddressEntry(ip, ip, "Address added and managed by OSC", Arrays.asList(tag));
        String element = QuickXmlizerUtil.xmlString(address);
        Map<String, String> queryStrings = makeSetConfigRequestParams(xpath, element, null);
        getRequest(queryStrings, GetAddressResponse.class);
    }

    public void removeTagFromAddress(String ip, String tag, String devGroup) throws Exception {
        LOG.info("Removing tag {} from address {}", tag, ip);
        String xpath = String.format(XPATH_ADDRESS_TAGMEMBER_TEMPL, devGroup, ip, tag);
        Map<String, String> queryStrings = makeDeleteConfigRequestParams(xpath, null, null);
        getRequest(queryStrings, GetAddressResponse.class);
    }

    public List<TagEntry> getTagEntries(String xpath) throws Exception {
        Map<String, String> queryStrings = makeGetConfigRequestParams(xpath, null, null);
        GetTagResponse getTagResponse = getRequest(queryStrings, GetTagResponse.class);
        List<TagEntry> tagEntries = getTagResponse.getResult().getEntries();
        return tagEntries != null ? tagEntries : emptyList();
    }

    public List<AddressEntry> getAddressEntries(String devGroup) throws Exception {
        String xpath = String.format(XPATH_ADDRESS_TEMPL, devGroup);
        Map<String, String> queryStrings = makeGetConfigRequestParams(xpath, null, null);
        GetAddressResponse getAddressResponse = getRequest(queryStrings, GetAddressResponse.class);
        List<AddressEntry> addrEntries = getAddressResponse.getAddressEntries();
        return addrEntries != null ? addrEntries : emptyList();
    }

    public boolean tagExists(String tagName, String xpath) throws Exception {
        return getTagEntries(xpath).stream().anyMatch(t -> t != null && Objects.equals(t.getName(), tagName));
    }

    public Map<String, String> makeSetConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(SET_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    private Map<String, String> makeGetConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(GET_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    public Map<String, String> makeDeleteConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(DELETE_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    public Map<String, String> makeOpCmdRequestParams(String cmd) {
        return makeRequestParams(null, OP_TYPE, null, null, cmd);
    }

    public Map<String, String> makeRequestParams(String action, String type, String xpath, String element, String cmd) {
        if (type == null) {
            throw new IllegalArgumentException("Type parameter required for panorama API requests!");
        }

        Map<String, String> queryStrings = new HashMap<>();

        queryStrings.put(TYPE, type);
        queryStrings.put(KEY, this.apiKey);

        if (action != null) {
            queryStrings.put(ACTION, action);
        }

        if (xpath != null) {
            queryStrings.put(XPATH, xpath);
        }

        if (element != null) {
            queryStrings.put(ELEMENT, element);
        }

        if (cmd != null) {
            queryStrings.put(CMD, cmd);
        }

        return queryStrings;
    }

    private UriBuilder getURIBuilder() {
        return UriBuilder.fromPath(PAN_REST_URL_BASE).scheme(this.scheme).host(this.host).port(this.port);
    }

    private Builder request(Map<String, String> inputParameters) {
        UriBuilder builder = getURIBuilder();

        for (String key : inputParameters.keySet()) {
            builder.queryParam(key, inputParameters.get(key));
        }

        return this.client.target(builder).request().accept(MediaType.APPLICATION_XML_TYPE);
    }

    public <T extends PANResponse> T getRequest(Map<String, String> queryStrings, Class<T> responseClass)
            throws InterruptedException, ExecutionException, TimeoutException {
        String status = FAILURE;
        T response = request(queryStrings).buildGet().submit(responseClass).get(60, SECONDS);
        status = response.getStatus();
        if (status.equals(SUCCESS)) {
            return response;
        } else {
            String errorMessage = String.format(
                    "Request failed for parameters %s with Status: %s;", queryStrings, status);
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    public static String makeEntryElement(String name) {
        return makeEntryElement(name, null, null, null);
    }

    public static String makeEntryElement(String name, String comments, String description, String color) {
        if (name == null) {
            throw new IllegalArgumentException(
                    String.format("Attempt to create an entry with null name! comment:%s; descr: %s; color: %s",
                            comments, description, color));
        }

        if (comments == null && description == null && color == null) {
            return String.format("<entry name=\"%s\"></entry>", name);
        }

        String commentsXml = comments != null ? String.format("<comments>%s</comments>", comments) : "";
        String descriptionXml = description != null ? String.format("<description>%s</description>", description) : "";
        String colorXml = color != null ? String.format("<color>%s</color>", color) : "";

        return String.format("<entry name=\"%s\">%s%s%s</entry>", name, commentsXml, descriptionXml, colorXml);
    }

    private String login(String loginName, String password) throws Exception {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put(USER, loginName);
        queryStrings.put(PASSWORD, password);
        queryStrings.put(TYPE, KEYGEN_TYPE);

        ShowResponse showResponse = getRequest(queryStrings, ShowResponse.class);

        String status = showResponse.getStatus();
        LOG.info("Login Status: {} & API Key = {}", status, showResponse.getShowResult().getKey());
        return showResponse.getShowResult().getKey();
    }

    private String getApiKey() {
        return this.apiKey;
    }
}
