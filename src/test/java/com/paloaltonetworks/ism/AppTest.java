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
package com.paloaltonetworks.ism;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.Ignore;
import org.junit.Test;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;
import com.paloaltonetworks.panorama.test.DeviceTest;

/**
 * Unit test for simple App.
 */
public class AppTest{
    private static final String PANORAMA_IP = "10.71.85.99";
    private static final String PANOS_SERIAL = "007299000003740";

    /**
     * Rigourous Test :-)
     *
     * @throws Exception
     */
    // Ignoring test since its environment specific, comment ignore to run the test
    @Ignore
    @Test
    public void testApp() throws Exception {
        assertTrue(true);

        String status = "failure";

        Client client = ClientBuilder.newBuilder().sslContext(DeviceTest.getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

        boolean isHttps = true;
        ShowOperations operations = new ShowOperations(PANORAMA_IP, 443, isHttps, "admin", "admin", client);

        // Get auth key
        String vmAuthKey = operations.getVMAuthKey("8760");
        System.out.println("VM Auth Key: " + vmAuthKey);

        ArrayList<String> devices = operations.showDevices();
        Iterator<String> deviceIterator = devices.iterator();
        while (deviceIterator.hasNext()) {
            System.out.println(deviceIterator.next());

        }

        // Check Connection
        boolean connectionCheck;
        connectionCheck = operations.checkConnection();
        System.out.println("Connection check status: " + connectionCheck);
        ArrayList<String> deviceGroups = operations.showDeviceGroups();
        Iterator<String> deviceGroupsIterator = deviceGroups.iterator();
        while (deviceGroupsIterator.hasNext()) {
            System.out.println(deviceGroupsIterator.next());

        }

        String dg = "testing";
        status = operations.deleteDeviceGroup(dg);
        if (status.equals("success")) {
            System.out.println("Successfully deleted device group: " + dg);
        }
        status = operations.addDeviceGroup(dg, "testing dg");
        if (status.equals("success")) {
            System.out.println("Successfully added device group: " + dg);
        }

        String dgTag = "testTag";
        //status = operations.DeleteDAGTag(dgTag);
        //if (status.equals("success")) {
        //    System.out.println("Successfully deleted TAG: " + dgTag);
        //}
        status = operations.addDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println("Successfully added TAG: " + dgTag);
        }
        status = operations.addDAG(dgTag, PANOS_SERIAL, Arrays.asList("13.13.13.13"));
        if (status.equals("success")) {
            System.out.println("Successfully added dynamic device group: " + dgTag);
        }

        //status = operations.DeleteDAG(dgTag, PANOS_SERIAL, "13.13.13.13");
        //if (status.equals("success")) {
        //   System.out.println("Successfully deleted dynamic device group: " + dgTag);
        // }

        boolean state = operations.TagExists(dgTag);
        if (state == true) {
            System.out.println(" tags " + dgTag + "exists");
        }

    }

}
