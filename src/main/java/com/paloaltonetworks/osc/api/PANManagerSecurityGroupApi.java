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

import com.intelsecurity.isc.plugin.manager.api.*;
import com.intelsecurity.isc.plugin.manager.element.ApplianceManagerConnectorElement;
import com.intelsecurity.isc.plugin.manager.element.ManagerSecurityGroupElement;
import com.intelsecurity.isc.plugin.manager.element.SecurityGroupMemberListElement;
import com.intelsecurity.isc.plugin.manager.element.VirtualSystemElement;
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
			showOperations = new ShowOperations(mc.getIpAddress(), mc.getUsername(), mc.getPassword());
			
			apiKey = showOperations.getApiKey();
			log.info("API Key is: "+ apiKey);
		}

		public static ManagerSecurityGroupApi create(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) throws Exception {
	        return new PANManagerSecurityGroupApi(mc,vs);
	    }
	    
	@Override
	public String createSecurityGroup(String name, String iscId,SecurityGroupMemberListElement memberList) throws Exception {
		String status = showOperations.AddDAGTag(name);
		if (status.equals("success")){
			return name;
		} else {
			return null;
			
		}
	}

	@Override
	public void updateSecurityGroup(String sgId, String name,SecurityGroupMemberListElement memberList) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSecurityGroup(String sgId) throws Exception {
		showOperations.DeleteDAGTag(sgId);
		
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