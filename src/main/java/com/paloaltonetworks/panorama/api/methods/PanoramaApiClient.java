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
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.mapping.CommitResponse;
import com.paloaltonetworks.panorama.api.mapping.DAGResponse;
import com.paloaltonetworks.panorama.api.mapping.DeviceEntry;
import com.paloaltonetworks.panorama.api.mapping.DeviceGroupResponse;
import com.paloaltonetworks.panorama.api.mapping.DeviceGroupsEntry;
import com.paloaltonetworks.panorama.api.mapping.GetAddressResponse;
import com.paloaltonetworks.panorama.api.mapping.GetTagResponse;
import com.paloaltonetworks.panorama.api.mapping.PANResponse;
import com.paloaltonetworks.panorama.api.mapping.SetConfigResponse;
import com.paloaltonetworks.panorama.api.mapping.ShowDeviceResponse;
import com.paloaltonetworks.panorama.api.mapping.ShowResponse;
import com.paloaltonetworks.panorama.api.mapping.TagEntry;
import com.paloaltonetworks.panorama.api.mapping.VMAuthKeyResponse;
import com.paloaltonetworks.utils.QuickXmlizerUtil;

public class PanoramaApiClient {

    private static final String SHOW_DEVICEGROUPS_CMD = "<show><devicegroups></devicegroups></show>";

    private static final Logger LOG = LoggerFactory.getLogger(PanoramaApiClient.class);

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String FAILURE = "failure";

    private static final String PAN_REST_URL_BASE = "/api/";

    // Http request paramenter names
    private static final String ELEMENT = "element";
    private static final String XPATH = "xpath";
    private static final String KEY = "key";
    private static final String TYPE = "type";
    private static final String ACTION = "action";
    private static final String CMD = "cmd";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    // Values for type parameter
    private static final String OP_TYPE = "op";
    private static final String USER_ID_TYPE = "user-id";
    private static final String CONFIG_TYPE = "config";
    private static final String KEYGEN_TYPE = "keygen";
    private static final String COMMIT_TYPE = "commit";

    // Values for action parameter
    private static final String SET_ACTION = "set";
    private static final String GET_ACTION = "get";
    private static final String EDIT_ACTION = "edit";
    private static final String DELETE_ACTION = "delete";

    // Values for xpath parameter
    private static final String XPATH_CONFIG = "/config";
    private static final String XPATH_SHARED = XPATH_CONFIG + "/shared";
    private static final String XPATH_SHARED_TAG = XPATH_SHARED + "/tag";

    private static final String XPATH_DEVGROUP_PREFIX = "/config/devices/entry[@name=\"localhost.localdomain\"]/device-group";
    private static final String XPATH_DEVGROUP_TEMPL = XPATH_DEVGROUP_PREFIX + "/entry[ @name=\"%s\"]";
    private static final String XPATH_SG_TAG_TEMPL = XPATH_DEVGROUP_TEMPL + "/tag";
    private static final String XPATH_ADDRESS_TEMPL = XPATH_DEVGROUP_TEMPL + "/address";
    private static final String XPATH_ADDRESS_GROUP_TEMPL = XPATH_DEVGROUP_TEMPL + "/address-group";
    private static final String XPATH_ADDRESS_TAG_TEMPL = XPATH_ADDRESS_TEMPL + "/entry[ @name='%s' ]/tag";

    private static final String GENKEY_CMD_TEMPL = "<request><bootstrap><vm-auth-key><generate><lifetime> %s"
            + "</lifetime></generate></vm-auth-key></bootstrap></request>";
    private static final String SHOW_DEVICES_ALL_CMD = "<show><devices><all></all></devices></show>";
    private static final String COMMIT_CMD = "<commit></commit>";

    // TODO : move out to a system property
    private static final String DEFAULT_OSC_DEVGROUP_NAME = "OpenSecurityController_Reserved";

    private final String host;
    private final int port;
    private final Client client;
    private final String scheme;
    private final String oscDevGroupName;
    private String apiKey;

    public PanoramaApiClient(String panServer, int port, boolean isHttps, String loginName, String password, Client client)
            throws Exception {
        this(panServer, port, isHttps, loginName, password, client, DEFAULT_OSC_DEVGROUP_NAME);
    }

    public PanoramaApiClient(String panServer, int port, boolean isHttps, String loginName, String password,
            Client client, String oscDevGroupName)
            throws Exception {
        this.host = panServer;
        this.port = port <= 0 ? isHttps ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT : port;
        this.scheme = isHttps ? HTTPS : HTTP;
        this.client = client;
        this.apiKey = login(loginName, password);
        this.oscDevGroupName = oscDevGroupName;
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
        Map<String, String> queryStrings = makeRequestParams(null, OP_TYPE, null, null, cmd);
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

    @SuppressWarnings("unchecked")
    public List<String> showDevices() throws Exception {
        Map<String, String> queryStrings = makeRequestParams(null, OP_TYPE, null, null, SHOW_DEVICES_ALL_CMD);
        ShowDeviceResponse showDeviceResponse = getRequest(queryStrings, ShowDeviceResponse.class);
        List<DeviceEntry> deviceGroups = emptyList();

        if (showDeviceResponse.getShowDeviceResult() != null){
            deviceGroups = showDeviceResponse.getShowDeviceResult().getShowDeviceEntries();
            deviceGroups = deviceGroups != null ? deviceGroups : emptyList();
        }

        LOG.info("Pan Devices List: " + deviceGroups);
        return deviceGroups.stream().filter(de -> de != null).map(de -> de.getName())
                            .collect(toList());
    }

    public List<String> showDeviceGroups() throws Exception {
        Map<String, String> queryStrings = makeRequestParams(null, OP_TYPE, null, null, SHOW_DEVICEGROUPS_CMD);
        List<DeviceGroupsEntry> deviceGroups = emptyList();
        DeviceGroupResponse deviceGroupResponse = getRequest(queryStrings, DeviceGroupResponse.class);

        if (deviceGroupResponse.getDeviceGroups() != null) {
            deviceGroups = deviceGroupResponse.getDeviceGroups().getEntries();
            deviceGroups = deviceGroups != null ? deviceGroups : emptyList();
        }

        List<String> panDeviceGroups = deviceGroups.stream().filter(dg -> dg != null).map(dg -> dg.getName())
                                                   .collect(toList());

        LOG.info("Pan Device Groups List: " + panDeviceGroups);

        return panDeviceGroups;
    }

    public String addDeviceGroup(String name, String description) throws Exception {
        LOG.info("Adding device group " + name);
        String element = makeEntryElement(name, null, description, null);
        Map<String, String> queryStrings = makeSetConfigRequestParams(XPATH_DEVGROUP_PREFIX, element, null);
        String status = getRequest(queryStrings, SetConfigResponse.class).getStatus();
        String errorMessage = String.format(
                "Commit failed when adding Device Group Name: %s and Description: %s ", name, description);
        configCommitOrThrow(errorMessage);
        return status;
    }

    public String deleteDeviceGroup(String name) throws Exception {
        String configStatus = FAILURE;

        String element = makeEntryElement(name);
        Map<String, String> queryStrings = makeRequestParams(DELETE_ACTION, CONFIG_TYPE, XPATH_DEVGROUP_PREFIX, element, null);
        SetConfigResponse setConfigResponse = getRequest(queryStrings, SetConfigResponse.class);
        configStatus = configCommit();
        if (configStatus.equals(ERROR)) {
            String errorMessage = String.format("Commit failed when deleting Device Group Name: %s ", name);
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }

        return setConfigResponse.getStatus();
    }

    public String addDAG(String name, List<String> tags) throws Exception {
        LOG.info("Adding Dynamic Address Group %s with tags: %s", name, tags);
        String xpath = String .format(XPATH_ADDRESS_GROUP_TEMPL, this.oscDevGroupName);
        String element = makeDynamicAddressGroupElement(name, tags);
        Map<String, String> queryStrings = makeSetConfigRequestParams(xpath, element, null);
        DAGResponse dagResponse = getRequest(queryStrings, DAGResponse.class);
        configCommit();
        return dagResponse.getStatus();
    }

    public String deleteDAG(String name) throws Exception {
        LOG.info("Deleting dynamic Address Group %s", name);
        String xpath = String .format(XPATH_ADDRESS_GROUP_TEMPL, this.oscDevGroupName);
        String element = makeEntryElement(name);
        Map<String, String> queryStrings = makeDeleteConfigRequestParams(xpath, element, null);
        DAGResponse dagResponse = getRequest(queryStrings, DAGResponse.class);
        configCommit();
        return dagResponse.getStatus();
    }

    public String addDAGTag(String name) throws Exception {
        String element = QuickXmlizerUtil.xmlString(new TagEntry(name, "color3", "OSC Tag"));
        Map<String, String> queryStrings = makeSetConfigRequestParams(XPATH_SHARED_TAG, element, null);
        SetConfigResponse setConfigResponse = getRequest(queryStrings, SetConfigResponse.class);
        configCommitOrThrow(String.format("Commit failed when adding DAGTag Name: %s ", name));
        return setConfigResponse.getStatus();
    }

    public boolean policyTagExists(String tagName) throws Exception {
        return tagExists(tagName, XPATH_SHARED_TAG);
    }

    public List<TagEntry> fetchPolicyTags() throws Exception {
        return getTagEntries(XPATH_SHARED_TAG).stream().collect(toList());
    }

    public List<AddressEntry> fetchAddresses() throws Exception {
        String xpath = String .format(XPATH_ADDRESS_TEMPL, this.oscDevGroupName);
        return getAddressEntries(xpath).stream().collect(toList());
    }

    public List<AddressEntry> fetchAddressesByPolicy(String policy) throws Exception {
        return fetchAddresses().stream().filter(a -> a.getTagNames() != null && a.getTagNames().contains(policy)).collect(toList());
    }

    public List<AddressEntry> fetchAddressesBySGTag(String sgTag) throws Exception {
        return fetchAddresses().stream().filter(a -> a.getTagNames() != null && a.getTagNames().contains(sgTag)).collect(toList());
    }

    public void addAddress(String ip) throws Exception {
        AddressEntry address = new AddressEntry(ip, ip, "Address added and managed by OSC", emptyList());
        String element = QuickXmlizerUtil.xmlString(address);
        String xpath = String.format(XPATH_ADDRESS_TEMPL, this.oscDevGroupName);

        Map<String, String> queryStrings = makeRequestParams(SET_ACTION, CONFIG_TYPE, xpath, element, null);
        getRequest(queryStrings, GetAddressResponse.class);
        configCommit();
    }

    public void deleteAddress(String ip) throws Exception {
        AddressEntry address = new AddressEntry(ip, null, null, null);
        String element = QuickXmlizerUtil.xmlString(address);
        String xpath = String.format(XPATH_ADDRESS_TEMPL, this.oscDevGroupName);
        Map<String, String> queryStrings = makeDeleteConfigRequestParams(xpath, element, null);
        getRequest(queryStrings, GetAddressResponse.class);
        configCommit();
    }

    public void addSGTag(String sgTag) throws Exception {
        String xpath = String .format(XPATH_SG_TAG_TEMPL, this.oscDevGroupName);
        String element = makeEntryElement(sgTag, "OSC Security group -- autocreated tag", null, null);
        Map<String, String> queryStrings = makeSetConfigRequestParams(xpath, element, null);
        getRequest(queryStrings, GetTagResponse.class);
        configCommit();
    }

    public void deleteSGTag(String sgTag) throws Exception {
        String xpath = String .format(XPATH_SG_TAG_TEMPL, this.oscDevGroupName);
        String element = makeEntryElement(sgTag);
        Map<String, String> queryStrings = makeDeleteConfigRequestParams(xpath, element, null);
        getRequest(queryStrings, GetTagResponse.class);
        configCommit();
    }

    public List<TagEntry> fetchSGTags() throws Exception {
        String xpath = String .format(XPATH_SG_TAG_TEMPL, this.oscDevGroupName);
        return getTagEntries(xpath).stream().collect(Collectors.toList());
    }

    public boolean sgTagExists(String sgTag) throws Exception {
        String xpath = String .format(XPATH_SG_TAG_TEMPL, this.oscDevGroupName);
        return tagExists(sgTag, xpath);
    }

    public void addSGTagToAddress(String ip, String sgTag) throws Exception {
        addTagToAddress(ip, sgTag);
    }

    public void removeSGTagFromAddress(String ip, String sgTag) throws Exception {
        removeTagFromAddress(ip, sgTag);
    }

    public void bindPolicyTagToAddress(String ip, String tag) throws Exception {
        addTagToAddress(ip, tag);
    }

    public void unbindPolicyTagFromAddress(String ip, String tag) throws Exception {
        removeTagFromAddress(ip, tag);
    }

    public String deleteDAGTag(String name) throws Exception {
        String element = makeEntryElement(name);
        Map<String, String> queryStrings = makeDeleteConfigRequestParams(XPATH_SHARED_TAG, element, null);
        SetConfigResponse setConfigResponse = getRequest(queryStrings, SetConfigResponse.class);
        configCommitOrThrow(String.format("Commit failed when deleting DAGTag Name: %s ", name));
        return setConfigResponse.getStatus();
    }

    private void configCommitOrThrow(String errorMessage) throws Exception {
        String configStatus = configCommit();
        if (configStatus.equals(ERROR)) {
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }
    }

    private void addTagToAddress(String ip, String tag) throws Exception {
        LOG.info("Adding tag %s to address %s", tag, ip);
        String xpath = String.format(XPATH_ADDRESS_TEMPL, this.oscDevGroupName);
        AddressEntry address = new AddressEntry(ip, ip, "Address added and managed by OSC", Arrays.asList(tag));
        String element = QuickXmlizerUtil.xmlString(address);
        Map<String, String> queryStrings = makeSetConfigRequestParams(xpath, element, null);
        getRequest(queryStrings, GetAddressResponse.class);
        configCommit();
    }

    private void removeTagFromAddress(String ip, String tag) throws Exception {
        LOG.info("Removing tag %s from address %s", tag, ip);
        List<AddressEntry> addresses = fetchAddress(ip);
        if (addresses == null || addresses.isEmpty()) {
            LOG.warn("Address %s does not exist. Cannot remove tag %s.", ip, tag);
            return;
        }

        // Need a paranoid check for multiple addresses with same name?
        AddressEntry address = addresses.get(0);
        List<String> tags = address.getTagNames();
        if (tags == null || tags.isEmpty()) {
            LOG.warn("Address %s has no tags. Cannot remove tag %s.", ip, tag);
            return;
        }

        tags = tags.stream().filter(t -> t != null && !t.equals(tag)).collect(toList());
        address.setTagNames(tags);

        String xpath = String.format(XPATH_ADDRESS_TAG_TEMPL, this.oscDevGroupName, ip);
        String element = QuickXmlizerUtil.xmlString(address);
        element = "<member>" + tag.trim() + "</member>";

        Map<String, String> queryStrings = makeDeleteConfigRequestParams(xpath, element, null);
        getRequest(queryStrings, GetAddressResponse.class);
        configCommit();
    }

    private List<TagEntry> getTagEntries(String xpath) throws Exception {
        Map<String, String> queryStrings = makeGetConfigRequestParams(xpath, null, null);
        GetTagResponse getTagResponse = getRequest(queryStrings, GetTagResponse.class);
        List<TagEntry> tagEntries = getTagResponse.getResult().getEntries();
        return tagEntries != null ? tagEntries : emptyList();
    }

    private List<AddressEntry> getAddressEntries(String xpath) throws Exception {
        Map<String, String> queryStrings = makeGetConfigRequestParams(xpath, null, null);
        GetAddressResponse getAddressResponse = getRequest(queryStrings, GetAddressResponse.class);
        List<AddressEntry> addrEntries = getAddressResponse.getResult().getEntries();
        return addrEntries != null ? addrEntries : emptyList();
    }

    private boolean tagExists(String tagName, String xpath) throws Exception {
        return getTagEntries(xpath).stream().anyMatch(t -> t != null && Objects.equals(t.getName(), tagName));
    }

    private List<AddressEntry> fetchAddress(String ip) throws Exception {
        AddressEntry address = new AddressEntry(ip, null, null, null);
        GetAddressResponse addrResponse = sendGetAddressRequest(address);
        return addrResponse.getResult().getEntries();
    }

    private GetAddressResponse sendGetAddressRequest(AddressEntry address) throws Exception {
        String element = QuickXmlizerUtil.xmlString(address);
        String xpath = String.format(XPATH_ADDRESS_TEMPL, this.oscDevGroupName);
        Map<String, String> queryStrings = makeGetConfigRequestParams(xpath, element, null);
        return getRequest(queryStrings, GetAddressResponse.class);
    }

    private GetAddressResponse sendSetAddressRequest(AddressEntry address) throws Exception {
        String element = QuickXmlizerUtil.xmlString(address);
        String xpath = String.format(XPATH_ADDRESS_TEMPL, this.oscDevGroupName);
        Map<String, String> queryStrings = makeSetConfigRequestParams(xpath, element, null);
        return getRequest(queryStrings, GetAddressResponse.class);
    }

    private GetTagResponse sendSharedTagRequest(String action, TagEntry tag) throws Exception {
        String element = QuickXmlizerUtil.xmlString(tag);
        String xpath = String.format(XPATH_SHARED_TAG, this.oscDevGroupName);

        Map<String, String> queryStrings = makeRequestParams(action, CONFIG_TYPE, xpath, element, null);

        return getRequest(queryStrings, GetTagResponse.class);
    }

    private Map<String, String> makeSetConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(SET_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    private Map<String, String> makeGetConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(GET_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    private Map<String, String> makeEditConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(EDIT_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    private Map<String, String> makeDeleteConfigRequestParams(String xpath, String element, String cmd) {
        return makeRequestParams(DELETE_ACTION, CONFIG_TYPE, xpath, element, cmd);
    }

    private Map<String, String> makeRequestParams(String action, String type, String xpath, String element, String cmd) {
        if (type == null) {
            throw new IllegalArgumentException("Type parameter required for panorama API requests!");
        }

        Map<String, String> queryStrings = new HashMap<>();

        if (action != null) {
            queryStrings.put(ACTION, action);
        }

        queryStrings.put(TYPE, type);
        queryStrings.put(KEY, this.apiKey);

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

    private String makeEntryElement(String name) {
        return makeEntryElement(name, null, null, null);
    }

    private String makeEntryElement(String name, String comments, String description, String color) {
        if (name == null) {
            throw new IllegalArgumentException(
                    String.format("Attempt to create an entry with null name! comment:%s; descr: %s; color: %s",
                            comments, description, color));
        }

        if (comments == null && description == null && color == null) {
            return String.format("<entry name=\"%s\"/>", name);
        }

        String commentsXml = comments != null ? String.format("<comments>%s</comments>", comments) : "";
        String descriptionXml = description != null ? String.format("<description>%s</description>", description) : "";
        String colorXml = color != null ? String.format("<color>%s</color>", color) : "";

        return String.format("<entry name=\"%s\"> %s %s %s </entry>", name, commentsXml, descriptionXml, colorXml);
    }

    private String makeDynamicAddressGroupElement(String name, List<String> tags) {
        String filter = tags.stream().map(s -> "'" + s + "'").reduce((s1, s2) -> (s1 + " or " + s2)).orElse("");
        String addressGroupTags = tags.stream().map(s -> ("<member>" + s + "</member>")).reduce((s1, s2) -> s1 + s2).orElse("");
        String element = String.format("<entry name=\"%s\"><dynamic><filter>%s</filter></dynamic>"
                                        + "<tag>%s</tag></entry>", name, filter, addressGroupTags);
        return element;
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

    private <T extends PANResponse> T getRequest(Map<String, String> queryStrings, Class<T> responseClass)
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

    private String configCommit() throws Exception {
        String status = FAILURE;

        Map<String, String> queryStrings = makeRequestParams(null, COMMIT_TYPE, null, null, COMMIT_CMD);

        try {
            CommitResponse commitResponse = getRequest(queryStrings, CommitResponse.class);
            status = commitResponse.getStatus();
            LOG.info(String.format("Commit Status : %s and Commit Code : %s", status, commitResponse.getCode()));
        } catch (Exception e) {
            LOG.error("Commit failed", e);
        }

        return status;
    }

    private String login(String loginName, String password) throws Exception {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put(USER, loginName);
        queryStrings.put(PASSWORD, password);
        queryStrings.put(TYPE, KEYGEN_TYPE);

        ShowResponse showResponse = getRequest(queryStrings, ShowResponse.class);

        String status = showResponse.getStatus();
        LOG.info("Login Status:" + status + " & API Key = " + showResponse.getShowResult().getKey());
        return showResponse.getShowResult().getKey();
    }

    private String getApiKey() {
        return this.apiKey;
    }
}
