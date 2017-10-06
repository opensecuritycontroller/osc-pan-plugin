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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;

import com.paloaltonetworks.panorama.api.mapping.CommitResponse;
import com.paloaltonetworks.panorama.api.mapping.DAGResponse;
import com.paloaltonetworks.panorama.api.mapping.DeviceEntry;
import com.paloaltonetworks.panorama.api.mapping.DeviceGroupResponse;
import com.paloaltonetworks.panorama.api.mapping.DeviceGroups;
import com.paloaltonetworks.panorama.api.mapping.DeviceGroupsEntry;
import com.paloaltonetworks.panorama.api.mapping.GetTagResponse;
import com.paloaltonetworks.panorama.api.mapping.SetConfigResponse;
import com.paloaltonetworks.panorama.api.mapping.ShowDeviceResponse;
import com.paloaltonetworks.panorama.api.mapping.ShowResponse;
import com.paloaltonetworks.panorama.api.mapping.TagEntry;
import com.paloaltonetworks.panorama.api.mapping.VMAuthKeyResponse;
import com.paloaltonetworks.utils.LogProvider;

public class ShowOperations {
    private static final Logger LOG = LogProvider.getLogger(ShowOperations.class);
    private static final String PAN_REST_URL_BASE = "/api/";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private final String host;
    private final int port;
    private final Client client;
    private final String scheme;

    private String apiKey;

    public ShowOperations(String panServer, int port, boolean isHttps, String loginName, String password, Client client)
            throws Exception {

        this.host = panServer;
        this.port = port <= 0 ? isHttps ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT : port;
        this.scheme = isHttps ? HTTPS : HTTP;
        this.client = client;
        this.apiKey = login(loginName, password);

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

    private <T> T getRequest(Map<String, String> inputParameters, Class<T> responseType)
            throws InterruptedException, ExecutionException, TimeoutException {

        return request(inputParameters).buildGet().submit(responseType).get(60, SECONDS);
    }

    private String login(String loginName, String password) throws Exception {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("user", loginName);
        queryStrings.put("password", password);
        queryStrings.put("type", "keygen");

        ShowResponse showResponse = this.getRequest(queryStrings, ShowResponse.class);

        String status = showResponse.getStatus();
        LOG.info("Login Status:" + status + " & API Key = " + showResponse.getShowResult().getKey());
        return showResponse.getShowResult().getKey();
    }

    private String getApiKey() {
        return this.apiKey;
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

        String vmAuthKey = null;
        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "op");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<request><bootstrap><vm-auth-key><generate><lifetime>" + days
                + "</lifetime></generate></vm-auth-key></bootstrap></request>");

        VMAuthKeyResponse vmAuthKeyResponse = null;
        vmAuthKeyResponse = this.getRequest(queryStrings, VMAuthKeyResponse.class);

        String status = vmAuthKeyResponse.getStatus();
        if (status.equals(SUCCESS)) {
            String vmAuthKeyString = vmAuthKeyResponse.getShowResult();
            /*
             * Parse out auth key
             */
            String[] temp;
            temp = vmAuthKeyString.split(" ");
            vmAuthKey = temp[3];
        } else {
            String errorMessage = String.format("Error generating VM Auth key for %s days", days);
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }

        return vmAuthKey;
    }

    public ArrayList<String> showDevices() throws Exception {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "op");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<show><devices><all></all></devices></show>");

        ShowDeviceResponse showDeviceResponse = null;
        showDeviceResponse = this.getRequest(queryStrings, ShowDeviceResponse.class);

        ArrayList<String> panDevices = new ArrayList<>();
        ArrayList<DeviceEntry> deviceEntries = showDeviceResponse.getShowDeviceResult().getShowDeviceEntry();

        LOG.info("Pan Devices List: " + deviceEntries);

        Iterator<DeviceEntry> deviceIterator = deviceEntries.iterator();
        while (deviceIterator.hasNext()) {
            String deviceSerial = deviceIterator.next().getName();
            panDevices.add(deviceSerial);
        }

        return panDevices;
    }

    public ArrayList<String> showDeviceGroups() throws Exception {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "op");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<show><devicegroups></devicegroups></show>");

        DeviceGroupResponse deviceGroupResponse = this.getRequest(queryStrings, DeviceGroupResponse.class);
        DeviceGroups deviceGroups = deviceGroupResponse.getDeviceGroups();

        ArrayList<String> panDeviceGroups = new ArrayList<>();
        ArrayList<DeviceGroupsEntry> deviceGroupsEntryList = deviceGroups.getEntry();
        Iterator<DeviceGroupsEntry> iterator = deviceGroupsEntryList.iterator();
        while (iterator.hasNext()) {
            String deviceGroupName = iterator.next().getName();
            panDeviceGroups.add(deviceGroupName);
        }

        LOG.info("Pan Device Groups List: " + panDeviceGroups);

        return panDeviceGroups;
    }

    public String addDeviceGroup(String name, String description) throws Exception {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "set");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/devices/entry[@name='localhost.localdomain']/device-group");
        queryStrings.put("element",
                "<entry name=\'" + name + "\'><description>" + description + "</description><devices/></entry>");

        SetConfigResponse setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);

        String status = "failure";
        String configStatus = "failure";

        status = setConfigResponse.getStatus();
        if (status.equals(SUCCESS)) {
            configStatus = configCommit();
            if (configStatus.equals(ERROR)) {
                String errorMessage = String.format(
                        "Commit failed when adding Device Group Name: %s and Description: %s ", name, description);
                LOG.error(errorMessage);
                throw new Exception(errorMessage);

            }
        } else {
            String errorMessage = String.format(
                    "Adding Device Group Name: %s and Description: %s failed with Status: %s, Code: %s and message: %s",
                    name, description, status, setConfigResponse.getCode(),
                    setConfigResponse.getConfigMessage().getMsg());
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }

        return status;
    }

    public String addDAG(String name, String panos_id, List<String> IPAddresses) throws Exception {

        String status = "failure";

        StringBuilder entries = new StringBuilder();
        for (String IPAddress : IPAddresses) {
            entries.append(String.format("<entry ip=\"%s\"><tag><member>%s</member></tag></entry>", IPAddress, name));
        }

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "set");
        queryStrings.put("type", "user-id");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("target", panos_id);
        queryStrings.put("cmd", "<uid-message><version>1.0</version><type>update</type><payload><register>"
                + entries.toString() + "</register></payload></uid-message>");

        DAGResponse dagResponse = this.getRequest(queryStrings, DAGResponse.class);

        status = dagResponse.getStatus();
        if (status.equals(SUCCESS)) {

            //TODO: Should the commit command be issued after success???????????
        } else {
            String errorMessage = String.format(
                    "Adding DAG with Name: %s, Pan Os Id: %s & Ip Addresses : %s failed with Status: %s", name,
                    panos_id, IPAddresses, status);
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }
        return status;
    }

    public String deleteDAG(String name, String panos_id, String IPAddress) throws Exception {

        String status = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "set");
        queryStrings.put("type", "user-id");
        queryStrings.put("target", panos_id);
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd",
                "<uid-message><version>1.0</version><type>update</type><payload><unregister>" + "<entry ip=\""
                        + IPAddress + "\"><tag><member>" + name + "</member></tag></entry>"
                        + "</unregister></payload></uid-message>");

        DAGResponse dagResponse = null;
        dagResponse = this.getRequest(queryStrings, DAGResponse.class);

        status = dagResponse.getStatus();
        if (status.equals(SUCCESS)) {

            //TODO: Should the commit command be issued after success???????????
        } else {
            String errorMessage = String.format(
                    "Deleting DAG with Name: %s, Pan Os Id: %s & IpAddress: %s failed with Status: %s", name, panos_id,
                    IPAddress, status);
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }
        return status;
    }

    public String addDAGTag(String name) throws Exception {

        String status = "failure";
        String configStatus = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "set");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/shared/tag");
        queryStrings.put("element",
                "<entry name=\'" + name + "\'><color>color3</color><comments>OSC Tag</comments></entry>");

        SetConfigResponse setConfigResponse = null;
        setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);

        status = setConfigResponse.getStatus();
        if (status.equals(SUCCESS)) {
            configStatus = configCommit();
            if (configStatus.equals(ERROR)) {
                String errorMessage = String.format("Commit failed when adding DAGTag Name: %s ", name);
                LOG.error(errorMessage);
                throw new Exception(errorMessage);
            }
        } else {
            String errorMessage = String.format(
                    "Adding DAGTag Name: %s failed with Status: %s, Code: %s and message: %s", name, status,
                    setConfigResponse.getCode(), setConfigResponse.getConfigMessage().getMsg());
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }

        return status;
    }

    public boolean TagExists(String tagName) throws Exception {

        String status = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "get");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/shared/tag");

        GetTagResponse getTagResponse = this.getRequest(queryStrings, GetTagResponse.class);

        status = getTagResponse.getStatus();

        if (status.equals(SUCCESS)) {
            ArrayList<TagEntry> tagEntry = getTagResponse.getTagResult().getEntry();
            if (tagEntry != null) {
                Iterator<TagEntry> tagIterator = tagEntry.iterator();
                while (tagIterator.hasNext()) {
                    if ((tagIterator.next().getName()).equals(tagName)) {
                        LOG.info(String.format("Tag Name: %s found", tagName));
                        return true;
                    }
                }
            }

        } else {
            String errorMessage = String.format("Searching Tag Name: %s failed with Status: %s & Code: %s", tagName,
                    status, getTagResponse.getCode());
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }
        return false;
    }

    public String deleteDAGTag(String name) throws Exception {

        String status = "failure";
        String configStatus = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "delete");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/shared/tag");
        queryStrings.put("element", "<entry name=\'" + name + "\'></entry>");

        SetConfigResponse setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);

        if (status.equals(SUCCESS)) {
            configStatus = configCommit();
            if (configStatus.equals(ERROR)) {
                String errorMessage = String.format("Commit failed when deleting DAGTag Name: %s ", name);
                LOG.error(errorMessage);
                throw new Exception(errorMessage);
            }
        } else {
            String errorMessage = String.format(
                    "Deleting DAGTag Name: %s failed with Status: %s, Code: %s and message: %s", name, status,
                    setConfigResponse.getCode(), setConfigResponse.getConfigMessage().getMsg());
            LOG.error(errorMessage);
            throw new Exception(errorMessage);

        }

        return status;
    }

    public String deleteDeviceGroup(String name) throws Exception {

        String status = "failure";
        String configStatus = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "delete");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/devices/entry[@name='localhost.localdomain']/device-group");
        queryStrings.put("element", "<entry name=\'" + name + "\'></entry>");

        SetConfigResponse setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);

        status = setConfigResponse.getStatus();
        if (status.equals(SUCCESS)) {
            configStatus = configCommit();
            if (configStatus.equals(ERROR)) {
                String errorMessage = String.format("Commit failed when deleting Device Group Name: %s ", name);
                LOG.error(errorMessage);
                throw new Exception(errorMessage);
            }
        } else {
            String errorMessage = String.format(
                    "Deleting Device Group Name: %s failed with Status: %s, Code: %s and message: %s", name, status,
                    setConfigResponse.getCode(), setConfigResponse.getConfigMessage().getMsg());
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }

        return status;
    }

    protected String configCommit() throws Exception {

        String status = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "commit");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<commit></commit>");

        CommitResponse commitResponse = this.getRequest(queryStrings, CommitResponse.class);

        status = commitResponse.getStatus();
        LOG.debug(String.format("Commit Status : %s and Commit Code : %s", status, commitResponse.getCode()));

        return status;
    }

}
