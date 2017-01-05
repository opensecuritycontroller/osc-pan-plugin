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
package com.paloaltonetworks.osc.api;

import java.util.List;

import org.apache.log4j.Logger;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;


/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi  {

    private static final Logger log = Logger.getLogger(PANManagerSecurityGroupInterfaceApi.class);
    static String apiKey = null;
    private VirtualSystemElement vs;
	private ApplianceManagerConnectorElement mc;
	private ShowOperations showOperations;

	public PANManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc,VirtualSystemElement vs, ShowOperations showOperations) {
    	this.vs = vs;
		this.mc = mc;
		this.showOperations = showOperations;
	}

	@Override
	public String createSecurityGroupInterface(String name, String policyId, String tag) throws Exception {
		String status = this.showOperations.addDAGTag(tag);
		if (status.equals("success")){
			return name;
		} else {
			return null;
		}
	}

	@Override
	public void updateSecurityGroupInterface(String id, String name, String policyId, String tag) throws Exception {

	}

	@Override
	public void deleteSecurityGroupInterface(String id) throws Exception {
		this.showOperations.deleteDAGTag(id);
	}

	@Override
	public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String id) throws Exception {

		return null;
	}

	@Override
	public String findSecurityGroupInterfaceByName(String name) throws Exception {

		return null;
	}

	@Override
	public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {

		return null;
	}

	@Override
	public void close() {

	}
}