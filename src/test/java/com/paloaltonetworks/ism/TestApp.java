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

import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;
import com.paloaltonetworks.panorama.test.DeviceTest;


public class TestApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        String status = "failure";

        Client client = ClientBuilder.newBuilder().sslContext(DeviceTest.getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

        boolean isHttps = true;
        ShowOperations operations = new ShowOperations("10.4.33.201", 443, isHttps, "admin", "admin", client);

	    ArrayList<String> devices = operations.ShowDevices();
	    Iterator<String> deviceIterator = devices.iterator();
		while (deviceIterator.hasNext()){
			System.out.println(deviceIterator.next());

		}

		ArrayList<String> deviceGroups = operations.ShowDeviceGroups();
	    Iterator<String> deviceGroupsIterator = deviceGroups.iterator();
		while (deviceGroupsIterator.hasNext()){
			System.out.println(deviceGroupsIterator.next());

		}

		String dg = "testing";
		status = operations.DeleteDeviceGroup(dg);
		if (status.equals("success")){
			System.out.println("Successfully deleted device group: " + dg);
		}
		status = operations.AddDeviceGroup(dg, "testing dg");
		if (status.equals("success")){
			System.out.println("Successfully added device group: " + dg);
		}
	}

}
