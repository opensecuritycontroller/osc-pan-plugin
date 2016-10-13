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



import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.SecurityGroupMemberElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;


/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupApi implements ManagerSecurityGroupApi  {


	 Logger log = Logger.getLogger(PANManagerSecurityGroupApi.class);
	    static String apiKey = null;
		VirtualSystemElement vs;
		ApplianceManagerConnectorElement mc;
		public ShowOperations showOperations = null;


	    private PANManagerSecurityGroupApi(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) {
	    	this.vs = vs;
			this.mc = mc;
			this.showOperations = new ShowOperations(mc.getIpAddress(), mc.getUsername(), mc.getPassword());

		}

		public static ManagerSecurityGroupApi create(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) throws Exception {
	        return new PANManagerSecurityGroupApi(mc,vs);
	    }

	@Override
	public String createSecurityGroup(String name, String iscId,SecurityGroupMemberListElement memberList) throws Exception {
		/*
		 * Check if tag exists - if not create
		 */
		boolean tagExists;
		String status;
		String pan_serial = null;
		
		tagExists = this.showOperations.TagExists(name);
		if (!tagExists){
			status = this.showOperations.AddDAGTag(name);
			if (!status.equals("success")){
				return null;
			}
		}
		 
		List<String> ipList = new ArrayList<>();

		for(SecurityGroupMemberElement member : memberList.getMembers()) {
		    ipList.addAll(member.getIpAddresses());
		}
		/*
		 * Add TAG and IP address
		 */
		ArrayList<String> pan_serialList = this.showOperations.ShowDevices();
		status = this.showOperations.AddDAG(name, pan_serialList.get(0), ipList);
		if (!status.equals("success")){
			return null;
		}
		return name;

	}

	@Override
	public void updateSecurityGroup(String sgId, String name,SecurityGroupMemberListElement memberList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteSecurityGroup(String sgId) throws Exception {
		this.showOperations.DeleteDAGTag(sgId);

	}

	@Override
	public List<? extends ManagerSecurityGroupElement> getSecurityGroupList() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ManagerSecurityGroupElement getSecurityGroupById(String sgId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}}