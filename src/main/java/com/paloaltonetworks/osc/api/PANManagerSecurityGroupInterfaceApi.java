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
import com.intelsecurity.isc.plugin.manager.element.ManagerSecurityGroupInterfaceElement;
import com.intelsecurity.isc.plugin.manager.element.VirtualSystemElement;
import com.paloaltonetworks.panorama.api.methods.ShowOperations;


/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi  {

	
    Logger log = Logger.getLogger(PANManagerSecurityGroupInterfaceApi.class);
    static String apiKey = null;
	VirtualSystemElement vs;
	ApplianceManagerConnectorElement mc;
	public ShowOperations showOperations = null;
  
    
    private PANManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) {
    	this.vs = vs;
		this.mc = mc;
		showOperations = new ShowOperations(mc.getIpAddress(), mc.getUsername(), mc.getPassword());
		
		apiKey = showOperations.getApiKey();
		log.info("API Key is: "+ apiKey);
	}

	public static ManagerSecurityGroupInterfaceApi create(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) throws Exception {
        return new PANManagerSecurityGroupInterfaceApi(mc,vs);
    }
    
  
    
	@Override
	public String createSecurityGroupInterface(String name, String policyId, String tag) throws Exception {
		String status = showOperations.AddDAGTag(tag);
		if (status.equals("success")){
			return name;
		} else {
			return null;
			
		}
	}

	@Override
	public void updateSecurityGroupInterface(String id, String name, String policyId, String tag) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSecurityGroupInterface(String id) throws Exception {
		showOperations.DeleteDAGTag(id);
		
	}

	@Override
	public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findSecurityGroupInterfaceByName(String name) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}