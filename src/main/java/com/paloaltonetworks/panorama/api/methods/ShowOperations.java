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

import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class ShowOperations {
    static Logger log = Logger.getLogger(ShowOperations.class);
    private URI baseUri;
    private String apiKey;

    public ShowOperations(String ipAddress, String username, String password) {

        this.baseUri = URI.create(String.format("https://%s/api", ipAddress));
        boolean sslCertDisabled = true;
        Client client = null;

        try {
            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            Map<String, String> queryStrings = new HashMap<String, String>();
            queryStrings.put("user", "admin");
            queryStrings.put("password", "admin");
            queryStrings.put("type", "keygen");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            //System.out.println(response.getStatus());
            ShowResponse showResponse = response.getEntity(ShowResponse.class);

            String status = showResponse.getStatus();
            String key = showResponse.getShowResult().getKey();
            System.out.println(status);
            System.out.println(key);
            this.apiKey = key;
            log.info("API Key is: "+ this.apiKey);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null) {
            client.destroy();
        }
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
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;
        Client client = null;
        String status = null;
        try {
            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("type", "op");
            queryStrings.put("key", apiKey);
            queryStrings.put("cmd", "<request><bootstrap><vm-auth-key><generate><lifetime>" + days
                    + "</lifetime></generate></vm-auth-key></bootstrap></request>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            //System.out.println(response.getStatus());
            VMAuthKeyResponse vmAuthKeyResponse = response.getEntity(VMAuthKeyResponse.class);

            status = vmAuthKeyResponse.getStatus();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null) {
            client.destroy();
        }
        return vmAuthKey;
    }

    public ArrayList<String> ShowDevices() {

        ArrayList<String> panDevices = new ArrayList<String>();
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;
        Client client = null;

        try {
            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("type", "op");
            queryStrings.put("key", apiKey);
            queryStrings.put("cmd", "<show><devices><all></all></devices></show>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            //System.out.println(response.getStatus());
            ShowDeviceResponse showDeviceResponse = response.getEntity(ShowDeviceResponse.class);

            //String status = showDeviceResponse.getStatus();
            //System.out.println(status);

            ArrayList<DeviceEntry> deviceEntry = showDeviceResponse.getShowDeviceResult().getShowDeviceEntry();
            System.out.println(deviceEntry.toString());
            String deviceSerial;
            Iterator<DeviceEntry> deviceIterator = deviceEntry.iterator();
            while (deviceIterator.hasNext()) {
                deviceSerial = deviceIterator.next().getName();
                panDevices.add(deviceSerial);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null) {
            client.destroy();
        }
        return panDevices;
    }

    public ArrayList<String> ShowDeviceGroups() {

        ArrayList<String> panDevices = new ArrayList<String>();
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;
        Client client = null;

        try {
            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("type", "op");
            queryStrings.put("key", apiKey);
            queryStrings.put("cmd", "<show><devicegroups></devicegroups></show>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println("Show Device Groups status : " + response.getStatus());
            //String strResponse = response.getEntity(String.class);
            //System.out.println("Response string: "+ strResponse);
            //System.out.println(response.getEntity(String.class));
            DeviceGroupResponse deviceGroupResponse = response.getEntity(DeviceGroupResponse.class);

            String status = deviceGroupResponse.getStatus();
            System.out.println("ShowDeviceGroups: " + status);

            DeviceGroups deviceGroup = deviceGroupResponse.getDeviceGroups();
            //System.out.println("Device Group: "+deviceGroup.toString());
            //System.out.println(deviceGroup.getEntry().
            //String deviceSerial;
            String deviceGroupName;
            //deviceGroupResponse.getDeviceGroups().

            ArrayList<DeviceGroupsEntry> deviceEntry = deviceGroup.getEntry();
            Iterator<DeviceGroupsEntry> deviceIterator = deviceEntry.iterator();
            while (deviceIterator.hasNext()) {
                deviceGroupName = deviceIterator.next().getName();
                System.out.println("Device Group Name: " + deviceGroupName);
                panDevices.add(deviceGroupName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null) {
            client.destroy();
        }
        return panDevices;
    }

    public String AddDeviceGroup(String name, String description) {

        String status = "failure";
        String configStatus = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "set");
            queryStrings.put("type", "config");
            queryStrings.put("key", apiKey);
            queryStrings.put("xpath", "/config/devices/entry[@name='localhost.localdomain']/device-group");
            queryStrings.put("element",
                    "<entry name=\'" + name + "\'><description>" + description + "</description><devices/></entry>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            SetConfigResponse setConfigResponse = response.getEntity(SetConfigResponse.class);

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
                System.out.println(
                        "AddDeviceGroup Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public String AddDAG(String name, String panos_id, List<String> IPAddresses) {

        String status = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        StringBuilder entries = new StringBuilder();
        for(String IPAddress: IPAddresses) {
            entries.append(
                    String.format("<entry ip=\"%s\"><tag><member>%s</member></tag></entry>", IPAddress, name));
        }


        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "set");
            queryStrings.put("type", "user-id");
            queryStrings.put("key", apiKey);
            queryStrings.put("target", panos_id);
            queryStrings.put("cmd",
            		"<uid-message><version>1.0</version><type>update</type><payload><register>" + entries.toString() + "</register></payload></uid-message>");
            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            DAGResponse dagResponse = response.getEntity(DAGResponse.class);

            status = dagResponse.getStatus();
            System.out.println("AddDAG Status: " + status);
            if (status.equals("success")) {

            } else {
                System.out.println("AddDAG Failed with status: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public String DeleteDAG(String name, String panos_id, String IPAddress) {

        String status = "failure";
        String configStatus = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "set");
            queryStrings.put("type", "user-id");
            queryStrings.put("target", panos_id);
            queryStrings.put("key", apiKey);
            queryStrings.put("cmd",
            		"<uid-message><version>1.0</version><type>update</type><payload><unregister>"+
            		"<entry ip=\"" + IPAddress + "\"><tag><member>"+name+"</member></tag></entry>"+
            		"</unregister></payload></uid-message>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            DAGResponse dagResponse = response.getEntity(DAGResponse.class);

            status = dagResponse.getStatus();
            System.out.println("DeleteDAG Status: " + status);
            if (status.equals("success")) {

            } else {
                System.out.println("DeleteDAG Failed with status: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
    public String AddDAGTag(String name) {

        String status = "failure";
        String configStatus = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "set");
            queryStrings.put("type", "config");
            queryStrings.put("key", apiKey);
            queryStrings.put("xpath", "/config/shared/tag");
            queryStrings.put("element",
                    "<entry name=\'" + name + "\'><color>color3</color><comments>OSC Tag</comments></entry>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            SetConfigResponse setConfigResponse = response.getEntity(SetConfigResponse.class);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
    public String ShowDAGTag(){

        String status = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "get");
            queryStrings.put("type", "config");
            queryStrings.put("key", apiKey);
            queryStrings.put("xpath", "/config/shared/tag");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            GetTagResponse getTagResponse = response.getEntity(GetTagResponse.class);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean TagExists(String tagName){

        String status = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "get");
            queryStrings.put("type", "config");
            queryStrings.put("key", apiKey);
            queryStrings.put("xpath", "/config/shared/tag");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            GetTagResponse getTagResponse = response.getEntity(GetTagResponse.class);
            status = getTagResponse.getStatus();
            System.out.println("AddDAGTag Status: " + status);
            if (status.equals("success")) {
                ArrayList<TagEntry> tagEntry = getTagResponse.getTagResult().getEntry();
                Iterator<TagEntry> tagIterator = tagEntry.iterator();
                while (tagIterator.hasNext()) {
                    if ((tagIterator.next().getName()).equals(tagName)){
                    	System.out.println("Matched Tag Name: " + tagName);
                    	return true;
                    }

                }

            } else {
                System.out.println("AddDAGTag Failed with status: " + status);
                System.out.println("AddDAGTag Failed with code: " + getTagResponse.getCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public String DeleteDAGTag(String name) {

        String status = "failure";
        String configStatus = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "delete");
            queryStrings.put("type", "config");
            queryStrings.put("key", apiKey);
            queryStrings.put("xpath", "/config/shared/tag");
            queryStrings.put("element", "<entry name=\'" + name + "\'></entry>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            SetConfigResponse setConfigResponse = response.getEntity(SetConfigResponse.class);

            status = setConfigResponse.getStatus();
            System.out.println("DeleteDAGTag Status: " + status);
            if (status.equals("success")) {
                configStatus = ConfigCommit();
                if (configStatus.equals("error")) {
                    System.out.println("Commit failed");
                }
            } else {
                System.out.println("DeleteDAGTag Failed with status: " + status);
                System.out.println("DeleteDAGTag Failed with code: " + setConfigResponse.getCode());
                System.out
                        .println("DeleteDAGTag Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public String DeleteDeviceGroup(String name) {

        String status = "failure";
        String configStatus = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("action", "delete");
            queryStrings.put("type", "config");
            queryStrings.put("key", apiKey);
            queryStrings.put("xpath", "/config/devices/entry[@name='localhost.localdomain']/device-group");
            queryStrings.put("element", "<entry name=\'" + name + "\'></entry>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            System.out.println(response.getStatus());
            SetConfigResponse setConfigResponse = response.getEntity(SetConfigResponse.class);

            status = setConfigResponse.getStatus();
            System.out.println("DeleteDeviceGroup Status: " + status);
            if (status.equals("success")) {
                configStatus = ConfigCommit();
                if (configStatus.equals("error")) {
                    System.out.println("Commit failed");
                }
            } else {
                System.out.println("DeleteDeviceGroup Failed with status: " + status);
                System.out.println("DeleteDeviceGroup Failed with code: " + setConfigResponse.getCode());
                System.out.println(
                        "DeleteDeviceGroup Failed with message: " + setConfigResponse.getConfigMessage().getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    protected String ConfigCommit() {

        String status = "failure";
        Map<String, String> queryStrings = new HashMap<String, String>();
        boolean sslCertDisabled = true;

        try {

            ClientConfig config = new DefaultClientConfig();
            sslConfiguration(sslCertDisabled, config);

            Client client = Client.create(config);
            WebResource webResource = client.resource(this.baseUri);

            String apiKey = getApiKey();
            queryStrings.put("type", "commit");
            queryStrings.put("key", apiKey);
            queryStrings.put("cmd", "<commit></commit>");

            for (String key : queryStrings.keySet()) {
                String value = queryStrings.get(key);
                webResource = webResource.queryParam(key, value);
            }
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);
            //System.out.println("Commit response: "+response.);
            System.out.println(response.getStatus());
            CommitResponse commitResponse = response.getEntity(CommitResponse.class);

            status = commitResponse.getStatus();
            System.out.println("Config Commit status: " + status);
            System.out.println("Config Commit code: " + commitResponse.getCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    private static void sslConfiguration(boolean sslCertDisabled, ClientConfig clientConfig) {
        if (!sslCertDisabled) {
            return;
        }
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");

            // Create a trust manager that does not validate certificate chains
            // against our server
            final TrustManager[] trustAllCerts;
            trustAllCerts = new TrustManager[] { new AcceptAllX509TrustManager() };
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            clientConfig.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                    new HTTPSProperties(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            // whatever your matching policy states
                            return true;
                        }
                    }, sslContext));
        } catch (Exception e) {
            log.warn("error creating SSL client", e);
            //Throwables.propagate(e);
        }
    }

    private static class AcceptAllX509TrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }
}
