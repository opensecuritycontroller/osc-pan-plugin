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

import org.junit.Test;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;

/**
 * Unit test for simple App.
 */
public class AppTest

{

    private static final String PANORAMA_IP = "10.4.33.201";

    /**
     * Rigourous Test :-)
     */
    // Ignoring test since its environment specific, comment ignore to run the test
//    @Ignore
    @Test
    public void testApp() {
        assertTrue(true);

        // TODO Auto-generated method stub
        String status = "failure";
        ShowOperations operations = new ShowOperations(PANORAMA_IP, "admin", "admin");

        // Get auth key
        String vmAuthKey = operations.getVMAuthKey("8760");
        System.out.println("VM Auth Key: " + vmAuthKey);

        ArrayList<String> devices = operations.ShowDevices();
        Iterator<String> deviceIterator = devices.iterator();
        while (deviceIterator.hasNext()) {
            System.out.println(deviceIterator.next());

        }

        // Check Connection
        boolean connectionCheck;
    	connectionCheck = operations.checkConnection();
    	System.out.println("Connection check status: " + connectionCheck);
        ArrayList<String> deviceGroups = operations.ShowDeviceGroups();
        Iterator<String> deviceGroupsIterator = deviceGroups.iterator();
        while (deviceGroupsIterator.hasNext()) {
            System.out.println(deviceGroupsIterator.next());

        }

        String dg = "testing";
        status = operations.DeleteDeviceGroup(dg);
        if (status.equals("success")) {
            System.out.println("Successfully deleted device group: " + dg);
        }
        status = operations.AddDeviceGroup(dg, "testing dg");
        if (status.equals("success")) {
            System.out.println("Successfully added device group: " + dg);
        }

        String dgTag = "testTAG";
        status = operations.DeleteDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println("Successfully deleted TAG: " + dgTag);
        }
        status = operations.AddDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println("Successfully added TAG: " + dgTag);
        }
        status = operations.AddDAG(dgTag, Arrays.asList("13.13.13.13"));
        if (status.equals("success")) {
            System.out.println("Successfully added dynamic device group: " + dgTag);
        }

        status = operations.DeleteDAG(dgTag,"13.13.13.13");
        if (status.equals("success")) {
            System.out.println("Successfully deleted dynamic device group: " + dgTag);
        }

        status = operations.ShowDAGTag();
        if (status.equals("success")) {
            System.out.println("Successfully displayed tags group: " + dgTag);
        }

        boolean state = operations.TagExists(dgTag);
        if (state == true) {
            System.out.println(" tags " + dgTag + "exists");
        }


    }

}
