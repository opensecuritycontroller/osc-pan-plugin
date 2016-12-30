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

import org.apache.log4j.Logger;

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

// TODO : Exception Handling
// TODO : Logging Activity
// TODO : Code clean up

public class ShowOperations {
    private static final Logger LOG = Logger.getLogger(ShowOperations.class);
    private static final String PAN_REST_URL_BASE = "/api/";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private final String host;
    private final int port;
    private final Client client;
    private final String scheme;

    private String apiKey;

    public ShowOperations(String panServer, int port, boolean isHttps, String loginName, String password, Client client) {

        this.host = panServer;
        this.port = port <= 0 ? isHttps ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT : port;
        this.scheme = isHttps ? HTTPS : HTTP;

        this.client = client;

        try {
            this.apiKey = login(loginName, password);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        //        UriBuilder uriBuilder = getURIBuilder();
        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("user", loginName);
        queryStrings.put("password", password);
        queryStrings.put("type", "keygen");

        //        for (String key : queryStrings.keySet()) {
        //            String value = queryStrings.get(key);
        //            uriBuilder.queryParam(key, value);
        //        }

        ShowResponse showResponse = this.getRequest(queryStrings, ShowResponse.class);
        //        ShowResponse showResponse = this.client.target(uriBuilder).request().accept(MediaType.APPLICATION_XML)
        //                .buildGet().submit(ShowResponse.class).get(60, TimeUnit.SECONDS);

        String status = showResponse.getStatus();

        System.out.println("Status:" + status + " & Key = " + showResponse.getShowResult().getKey());
        LOG.info("API Key is: " + this.apiKey);
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

    public String getVMAuthKey(String days) {

        String vmAuthKey = null;
        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "op");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<request><bootstrap><vm-auth-key><generate><lifetime>" + days
                + "</lifetime></generate></vm-auth-key></bootstrap></request>");

        VMAuthKeyResponse vmAuthKeyResponse = null;
        try {

            vmAuthKeyResponse = this.getRequest(queryStrings, VMAuthKeyResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        String status = vmAuthKeyResponse.getStatus();
        System.out.println(status);
        if (status.equals("success")) {
            String vmAuthKeyString = vmAuthKeyResponse.getShowResult();
            /*
             * Parse out auth key
             */
            String[] temp;
            temp = vmAuthKeyString.split(" ");
            vmAuthKey = temp[3];
        } else {
            System.out.println("Error generation vm auth key");
        }

        return vmAuthKey;
    }

    public ArrayList<String> ShowDevices() {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "op");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<show><devices><all></all></devices></show>");

        ShowDeviceResponse showDeviceResponse = null;
        try {
            showDeviceResponse = this.getRequest(queryStrings, ShowDeviceResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        ArrayList<String> panDevices = new ArrayList<>();
        ArrayList<DeviceEntry> deviceEntry = showDeviceResponse.getShowDeviceResult().getShowDeviceEntry();
        System.out.println(deviceEntry.toString());
        String deviceSerial;
        Iterator<DeviceEntry> deviceIterator = deviceEntry.iterator();
        while (deviceIterator.hasNext()) {
            deviceSerial = deviceIterator.next().getName();
            panDevices.add(deviceSerial);
        }

        return panDevices;
    }

    public ArrayList<String> ShowDeviceGroups() {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "op");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<show><devicegroups></devicegroups></show>");

        DeviceGroupResponse deviceGroupResponse = null;
        try {
            deviceGroupResponse = this.getRequest(queryStrings, DeviceGroupResponse.class);
            //            deviceGroupResponse = this.client.target(uriBuilder).request().accept(MediaType.APPLICATION_XML)
            //                    .buildGet().submit(DeviceGroupResponse.class).get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        String status = deviceGroupResponse.getStatus();
        System.out.println("ShowDeviceGroups: " + status);

        DeviceGroups deviceGroup = deviceGroupResponse.getDeviceGroups();
        //System.out.println("Device Group: "+deviceGroup.toString());
        //System.out.println(deviceGroup.getEntry().
        //String deviceSerial;
        String deviceGroupName;
        //deviceGroupResponse.getDeviceGroups().

        ArrayList<String> panDevices = new ArrayList<>();
        ArrayList<DeviceGroupsEntry> deviceEntry = deviceGroup.getEntry();
        Iterator<DeviceGroupsEntry> deviceIterator = deviceEntry.iterator();
        while (deviceIterator.hasNext()) {
            deviceGroupName = deviceIterator.next().getName();
            System.out.println("Device Group Name: " + deviceGroupName);
            panDevices.add(deviceGroupName);
        }
        return panDevices;
    }

    public String AddDeviceGroup(String name, String description) {

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "set");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/devices/entry[@name='localhost.localdomain']/device-group");
        queryStrings.put("element",
                "<entry name=\'" + name + "\'><description>" + description + "</description><devices/></entry>");

        SetConfigResponse setConfigResponse = null;
        try {
            setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        String status = "failure";
        String configStatus = "failure";

        status = setConfigResponse.getStatus();
        System.out.println("AddDeviceGroup Status: " + status);
        if (status.equals("success")) {
            configStatus = ConfigCommit();
            if (configStatus.equals("error")) {
                System.out.println("Commit failed");
            }
        } else {
            System.out.println("AddDeviceGroup Failed with status: " + status);
            System.out.println("AddDeviceGroup Failed with code: " + setConfigResponse.getCode());
            System.out.println("AddDeviceGroup Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
        }

        return status;
    }

    public String AddDAG(String name, String panos_id, List<String> IPAddresses) {

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

        DAGResponse dagResponse = null;
        try {
            dagResponse = this.getRequest(queryStrings, DAGResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        status = dagResponse.getStatus();
        System.out.println("AddDAG Status: " + status);
        if (status.equals("success")) {

        } else {
            System.out.println("AddDAG Failed with status: " + status);
        }
        return status;
    }

    public String DeleteDAG(String name, String panos_id, String IPAddress) {

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
        try {
            dagResponse = this.getRequest(queryStrings, DAGResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        status = dagResponse.getStatus();
        System.out.println("DeleteDAG Status: " + status);
        if (status.equals("success")) {

        } else {
            System.out.println("DeleteDAG Failed with status: " + status);
        }
        return status;
    }

    public String AddDAGTag(String name) {

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
        try {
            setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        status = setConfigResponse.getStatus();
        System.out.println("AddDAGTag Status: " + status);
        if (status.equals("success")) {
            configStatus = ConfigCommit();
            if (configStatus.equals("error")) {
                System.out.println("Commit failed");
            }
        } else {
            System.out.println("AddDAGTag Failed with status: " + status);
            System.out.println("AddDAGTag Failed with code: " + setConfigResponse.getCode());
            System.out.println("AddDAGTag Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
        }

        return status;
    }

    public String ShowDAGTag() {

        String status = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "get");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/shared/tag");

        GetTagResponse getTagResponse = null;
        try {
            getTagResponse = this.getRequest(queryStrings, GetTagResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        String tagName;
        status = getTagResponse.getStatus();
        System.out.println("AddDAGTag Status: " + status);
        if (status.equals("success")) {
            ArrayList<TagEntry> tagEntry = getTagResponse.getTagResult().getEntry();
            Iterator<TagEntry> tagIterator = tagEntry.iterator();
            while (tagIterator.hasNext()) {
                tagName = tagIterator.next().getName();
                System.out.println("Tag Name: " + tagName);
            }

        } else {
            System.out.println("AddDAGTag Failed with status: " + status);
            System.out.println("AddDAGTag Failed with code: " + getTagResponse.getCode());
        }

        return status;
    }

    public boolean TagExists(String tagName) {

        String status = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "get");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/shared/tag");

        GetTagResponse getTagResponse = null;
        try {
            getTagResponse = this.getRequest(queryStrings, GetTagResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        status = getTagResponse.getStatus();
        System.out.println("AddDAGTag Status: " + status);
        if (status.equals("success")) {
            ArrayList<TagEntry> tagEntry = getTagResponse.getTagResult().getEntry();
            Iterator<TagEntry> tagIterator = tagEntry.iterator();
            while (tagIterator.hasNext()) {
                if ((tagIterator.next().getName()).equals(tagName)) {
                    System.out.println("Matched Tag Name: " + tagName);
                    return true;
                }

            }

        } else {
            System.out.println("AddDAGTag Failed with status: " + status);
            System.out.println("AddDAGTag Failed with code: " + getTagResponse.getCode());
        }
        return false;
    }

    public String DeleteDAGTag(String name) {

        String status = "failure";
        String configStatus = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "delete");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/shared/tag");
        queryStrings.put("element", "<entry name=\'" + name + "\'></entry>");

        SetConfigResponse setConfigResponse = null;
        try {
            setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        if (status.equals("success")) {
            configStatus = ConfigCommit();
            if (configStatus.equals("error")) {
                System.out.println("Commit failed");
            }
        } else {
            System.out.println("DeleteDAGTag Failed with status: " + status);
            System.out.println("DeleteDAGTag Failed with code: " + setConfigResponse.getCode());
            System.out.println("DeleteDAGTag Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
        }

        return status;
    }

    public String DeleteDeviceGroup(String name) {

        String status = "failure";
        String configStatus = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("action", "delete");
        queryStrings.put("type", "config");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("xpath", "/config/devices/entry[@name='localhost.localdomain']/device-group");
        queryStrings.put("element", "<entry name=\'" + name + "\'></entry>");

        SetConfigResponse setConfigResponse = null;
        try {
            setConfigResponse = this.getRequest(queryStrings, SetConfigResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        status = setConfigResponse.getStatus();
        if (status.equals("success")) {
            configStatus = ConfigCommit();
            if (configStatus.equals("error")) {
                System.out.println("Commit failed");
            }
        } else {
            System.out.println("DeleteDeviceGroup Failed with status: " + status);
            System.out.println("DeleteDeviceGroup Failed with code: " + setConfigResponse.getCode());
            System.out
                    .println("DeleteDeviceGroup Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
        }

        return status;
    }

    protected String ConfigCommit() {

        String status = "failure";

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("type", "commit");
        queryStrings.put("key", this.apiKey);
        queryStrings.put("cmd", "<commit></commit>");

        CommitResponse commitResponse = null;
        try {
            commitResponse = this.getRequest(queryStrings, CommitResponse.class);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (TimeoutException e) {

            e.printStackTrace();
        }

        status = commitResponse.getStatus();
        System.out.println("Config Commit status: " + status);
        System.out.println("Config Commit code: " + commitResponse.getCode());

        return status;
    }

}
