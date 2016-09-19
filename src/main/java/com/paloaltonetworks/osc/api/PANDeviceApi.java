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



import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.element.ApplianceBootstrapInformationElement;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.BootStrapInfoProviderElement;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.osc.model.Device;
import com.paloaltonetworks.panorama.api.methods.ShowOperations;
import com.sun.jersey.core.util.Base64;

/**
 * This documents "Device Management Apis"
 */
public class PANDeviceApi implements ManagerDeviceApi  {

	static Logger log = Logger.getLogger(PANDeviceApi.class);
	static String apiKey = null;
	static String vmAuthKey = null;
	VirtualSystemElement vs;
	ApplianceManagerConnectorElement mc;
	public ShowOperations showOperations = null;

	public static ManagerDeviceApi create(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) throws Exception {

		return new PANDeviceApi(mc, vs);
	}

	private PANDeviceApi(ApplianceManagerConnectorElement mc,VirtualSystemElement vs) {
		this.vs = vs;
		this.mc = mc;
		this.showOperations = new ShowOperations(mc.getIpAddress(), mc.getUsername(), mc.getPassword());
		vmAuthKey = this.showOperations.getVMAuthKey("8760");
	}

	@Override
	public boolean isDeviceGroupSupported() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ManagerDeviceElement getDeviceById(String id) throws Exception {
		ArrayList<String> deviceGroups = this.showOperations.ShowDeviceGroups();
		for (String deviceGroupName : deviceGroups) {
			if (deviceGroupName.equals(id)) {
				System.out.println("Device Group Name: " + deviceGroupName);
				return new Device(deviceGroupName, deviceGroupName);

			}
		}

		return null;
	}

	@Override
	public String findDeviceByName(String name) throws Exception {
		return getDeviceById(name) == null ? null : name;
	}

	@Override
	public List<? extends ManagerDeviceElement> listDevices() throws Exception {
		// TODO Auto-generated method stub
		List<Device> deviceGroups = new ArrayList<Device>();
		ArrayList<String> panDeviceGroups = this.showOperations.ShowDeviceGroups();
		for(String deviceGroupName: panDeviceGroups){
			System.out.println("Device Group Name: "+deviceGroupName);
			deviceGroups.add(new Device(deviceGroupName, deviceGroupName));
		}
		return deviceGroups;
	}

	@Override
	public String createVSSDevice() throws Exception {
		// TODO Auto-generated method stub
		// Create a device group in panorama
		// Information passed in by VSS to create device group
		// VSS is the device group

		return this.showOperations.AddDeviceGroup(this.vs.getName(), this.vs.getName());
	}

	@Override
	public void updateVSSDevice(ManagerDeviceElement device) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteVSSDevice() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String createDeviceMember(String name, String vserverIpAddress, String contactIpAddress, String ipAddress,
			String gateway, String prefixLength) throws Exception {
		// TODO Auto-generated method stub
		// OSC calls this method to create a NGFW - pass this to panorama
		// Return panoamora device id
		String panDeviceGroup = this.vs.getMgrId();

		return null;
	}

	@Override
	public String updateDeviceMember(ManagerDeviceMemberElement deviceElement, String name, String deviceHostName,
			String ipAddress, String mgmtIPAddress, String gateway, String prefixLength) throws Exception {
		// TODO Auto-generated method stub
		// Redeploy need to thing through
		return null;
	}

	@Override
	public void deleteDeviceMember(String id) throws Exception {
		// TODO Auto-generated method stub
		// Delete a firewall
	}

	@Override
	public ManagerDeviceMemberElement getDeviceMemberById(String id) throws Exception {
		// TODO Auto-generated method stub
		// return device from panorma
		return null;
	}

	@Override
	public ManagerDeviceMemberElement findDeviceMemberByName(String name) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends ManagerDeviceMemberElement> listDeviceMembers() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUpgradeSupported(String modelType, String prevSwVersion, String newSwVersion) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getDeviceMemberConfigById(String mgrDeviceId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getDeviceMemberConfiguration(DistributedApplianceInstanceElement dai) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getDeviceMemberAdditionalConfiguration(DistributedApplianceInstanceElement dai) {
		// TODO Auto-generated method stub
		return null;
	}


	protected byte[] readFile(String path, Charset encoding)
			  throws IOException
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return Base64.encode(new String(encoded, encoding));
			}

	@Override
	public ApplianceBootstrapInformationElement getBootstrapinfo(BootStrapInfoProviderElement bootStrapInfo) {

		PANApplianceBootstrapInformationElement bootstrapElement = new PANApplianceBootstrapInformationElement();
		byte [] nullEntry = Base64.encode("");
		try {
			bootstrapElement.addBootstrapFile("/config/init-cfg.txt",readFile("/home/stack/work/config/init_cfg.txt",StandardCharsets.UTF_8));

		bootstrapElement.addBootstrapFile("/config/bootstrap.xml",readFile("/home/stack/work/config/bootstrap.xml",StandardCharsets.UTF_8));
		bootstrapElement.addBootstrapFile("/license/authcodes",readFile("/home/stack/work/config/authcode.txt",StandardCharsets.UTF_8));
		bootstrapElement.addBootstrapFile("/content",nullEntry);
		bootstrapElement.addBootstrapFile("/software",nullEntry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bootstrapElement;
	}

/*
	@Override
	public byte[] getBootstrapinfo(ApplianceBootStrapElement bootStrapInfo) {
		// TODO Auto-generated method stub
		// Pass in bootstrap info for device
		// Same info for all devices
		// use vss element to determine device group
		// give me config drive info for device group
		StringBuilder configString = new StringBuilder();

		configString.append("type=dhcp-client"+System.lineSeparator());
		configString.append("ip-address="+System.lineSeparator());
		configString.append("default-gateway="+System.lineSeparator());
		configString.append("netmask="+System.lineSeparator());
		configString.append("ipv6-address="+System.lineSeparator());
		configString.append("ipv6-default-gateway="+System.lineSeparator());
		configString.append("hostname="+System.lineSeparator());
		configString.append("panorama-server=10.4.33.201"+System.lineSeparator());
		configString.append("panorama-server-2="+System.lineSeparator());
		configString.append("tplname="+System.lineSeparator());
		configString.append("dgname="+System.lineSeparator());
		configString.append("dns-primary=10.44.2.10"+System.lineSeparator());
		configString.append("dns-secondary=10.43.2.10"+System.lineSeparator());
		configString.append("op-command-modes="+System.lineSeparator());
		configString.append("dhcp-send-hostname=yes"+System.lineSeparator());
		configString.append("dhcp-send-client-id=yes"+System.lineSeparator());
		configString.append("dhcp-accept-server-hostname=yes"+System.lineSeparator());
		configString.append("dhcp-accept-server-domain=yes"+System.lineSeparator());
		configString.append("vm-auth-key="+this.vmAuthKey+System.lineSeparator());

		return Base64.encode(configString.toString());
	}
*/
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}


}