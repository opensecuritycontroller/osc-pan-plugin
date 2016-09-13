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

import org.apache.log4j.Logger;

import com.intelsecurity.isc.plugin.manager.ManagerAuthenticationType;
import com.intelsecurity.isc.plugin.manager.ManagerNotificationSubscriptionType;
import com.intelsecurity.isc.plugin.manager.api.ApplianceManagerApi;
import com.intelsecurity.isc.plugin.manager.api.IscJobNotificationApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerCallbackNotificationApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerDeviceApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerDomainApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerPolicyApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerSecurityGroupApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerSecurityGroupInterfaceApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerWebSocketNotificationApi;
import com.intelsecurity.isc.plugin.manager.element.ApplianceManagerConnectorElement;
import com.intelsecurity.isc.plugin.manager.element.VirtualSystemElement;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;

public class PANApplianceManagerApi implements ApplianceManagerApi {

	Logger log = Logger.getLogger(PANApplianceManagerApi.class);

	public PANApplianceManagerApi() {

	}

	public static PANApplianceManagerApi create() {
		return new PANApplianceManagerApi();
	}

	@Override
	public ManagerDeviceApi createManagerDeviceApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs)
			throws Exception {
		// TODO Auto-generated method stub
		log.info("Username: " + mc.getUsername());
		System.out.println("Username: " + mc.getUsername());
		// add mc to create
		return PANDeviceApi.create(mc, vs);
	}

	@Override
	public ManagerSecurityGroupInterfaceApi createManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc,
			VirtualSystemElement vs) throws Exception {
		
		//return PANManagerSecurityGroupInterfaceApi.create(mc,vs);
		return null;
	}

	@Override
	public ManagerSecurityGroupApi createManagerSecurityGroupApi(ApplianceManagerConnectorElement mc,
			VirtualSystemElement vs) throws Exception {
		// TODO Auto-generated method stub
		//return PANManagerSecurityGroupApi.create(mc,vs);
		return null;
	}

	@Override
	public ManagerPolicyApi createManagerPolicyApi(ApplianceManagerConnectorElement mc) throws Exception {
		// TODO Auto-generated method stub
		log.info("Creating Policy API");
		return PANManagerPolicyApi.create(mc);
		//return null;
	}

	@Override
	public ManagerDomainApi createManagerDomainApi(ApplianceManagerConnectorElement mc) throws Exception {
		 return PANManagerDomainApi.create(mc);
		 //return null;
	}

	@Override
	public byte[] getPublicKey(ApplianceManagerConnectorElement mc) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "PANMgrPlugin";
	}

	@Override
	public String getVendorName() {
		// TODO Auto-generated method stub
		return "Palo Alto Networks";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getServiceName() {
		return "PANMgrPlugin";
	}

	@Override
	public String getNsxServiceName() {
		return "Pan-nsx";
	}

	@Override
	public String getManagerUrl(String ipAddress) {
		return "https://" + ipAddress;
	}

	@Override
	public ManagerAuthenticationType getAuthenticationType() {
		return ManagerAuthenticationType.BASIC_AUTH;
	}

	@Override
	public boolean isSecurityGroupSyncSupport() {
		// TODO Change this
		return false;
	}

	@Override
	public void checkConnection(ApplianceManagerConnectorElement mc) throws Exception {
		// TODO Auto-generated method stub
		// TODO ADD
		boolean connectionCheck = false;

		ShowOperations showOperations = new ShowOperations(mc.getIpAddress(), mc.getUsername(), mc.getPassword());
		connectionCheck = showOperations.checkConnection();
		if (connectionCheck == false) {
			throw new Exception("Connection Check failed ");
		}
	}

	@Override
	public boolean isAgentManaged() {
		return false;
	}
	
	@Override
    public boolean isPolicyMappingSupported() {
    	return false;
    }

	@Override
	public ManagerWebSocketNotificationApi createManagerWebSocketNotificationApi(ApplianceManagerConnectorElement mc)
			throws Exception {
		throw new UnsupportedOperationException("WebSocket Notification not implemented");
	}

	@Override
	public ManagerCallbackNotificationApi createManagerCallbackNotificationApi(ApplianceManagerConnectorElement mc)
			throws Exception {
		 throw new UnsupportedOperationException("Manager does not support notification");
	}

	@Override
	public IscJobNotificationApi createIscJobNotificationApi(ApplianceManagerConnectorElement mc,
			VirtualSystemElement vs) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ManagerNotificationSubscriptionType getNotificationType() {
		 return ManagerNotificationSubscriptionType.NONE;
	}

	
}
