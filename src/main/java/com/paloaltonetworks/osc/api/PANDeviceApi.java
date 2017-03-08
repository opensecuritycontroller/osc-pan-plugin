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
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
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

/**
 * This documents "Device Management Apis"
 */
public class PANDeviceApi implements ManagerDeviceApi  {

    // "I4745132";"I2306077";
	private static final String LICENSE_AUTH_CODE = "I7517916";
    private static final Logger LOG = Logger.getLogger(PANDeviceApi.class);
	static String apiKey = null;
	static String vmAuthKey = null;
	private VirtualSystemElement vs;
	private ApplianceManagerConnectorElement mc;
	private ShowOperations showOperations;
	private static final String daysforVMAuthKey = "8760";

	public PANDeviceApi(ApplianceManagerConnectorElement mc,VirtualSystemElement vs,  ShowOperations showOperations) throws Exception {
		this.vs = vs;
		this.mc = mc;
		this.showOperations = showOperations;
		vmAuthKey = this.showOperations.getVMAuthKey(daysforVMAuthKey);
	}

	@Override
	public boolean isDeviceGroupSupported() {
		return true;
	}

	@Override
	public ManagerDeviceElement getDeviceById(String id) throws Exception {
		ArrayList<String> deviceGroups = this.showOperations.showDeviceGroups();
		for (String deviceGroupName : deviceGroups) {
			if (deviceGroupName.equals(id)) {
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

		List<Device> deviceGroups = new ArrayList<>();
		ArrayList<String> panDeviceGroups = this.showOperations.showDeviceGroups();
		for(String deviceGroupName: panDeviceGroups){
			deviceGroups.add(new Device(deviceGroupName, deviceGroupName));
		}
		return deviceGroups;
	}

	@Override
	public String createVSSDevice() throws Exception {
		// Create a device group in panorama
		// Information passed in by VSS to create device group
		// VSS is the device group

		return this.showOperations.addDeviceGroup(this.vs.getName(), this.vs.getName());
	}

	@Override
	public void updateVSSDevice(ManagerDeviceElement device) throws Exception {

	}

	@Override
	public void deleteVSSDevice() throws Exception {


	}

	@Override
	public String createDeviceMember(String name, String vserverIpAddress, String contactIpAddress, String ipAddress,
			String gateway, String prefixLength) throws Exception {

		// OSC calls this method to create a NGFW - pass this to panorama
		// Return panorqma device id
		return name;
	}

	@Override
	public String updateDeviceMember(ManagerDeviceMemberElement deviceElement, String name, String deviceHostName,
			String ipAddress, String mgmtIPAddress, String gateway, String prefixLength) throws Exception {

		// Redeploy need to thing through
		return null;
	}

	@Override
	public void deleteDeviceMember(String id) throws Exception {

		// Delete a firewall
	}

	@Override
	public ManagerDeviceMemberElement getDeviceMemberById(String id) throws Exception {

		// return device from panorma
		return null;
	}

	@Override
	public ManagerDeviceMemberElement findDeviceMemberByName(String name) throws Exception {

		return null;
	}

	@Override
	public List<? extends ManagerDeviceMemberElement> listDeviceMembers() throws Exception {

		return Collections.emptyList();
	}

	@Override
	public boolean isUpgradeSupported(String modelType, String prevSwVersion, String newSwVersion) throws Exception {

		return false;
	}

	@Override
	public byte[] getDeviceMemberConfigById(String mgrDeviceId) throws Exception {

		return null;
	}

	@Override
	public byte[] getDeviceMemberConfiguration(DistributedApplianceInstanceElement dai) {

		return null;
	}

	@Override
	public byte[] getDeviceMemberAdditionalConfiguration(DistributedApplianceInstanceElement dai) {

		return null;
	}


	protected byte[] readFile(String path, Charset encoding)
			  throws IOException
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return Base64.encodeBase64(new String(encoded, encoding).getBytes());
			}
/*
	protected byte[] getInitCfg(){
		byte[] encoded;
		String initCfgStr = null;

		encoded = initCfgStr.getBytes(StandardCharsets.UTF_8);
		return Base64.encode(encoded);
	}
	*/
	@Override
	public ApplianceBootstrapInformationElement getBootstrapinfo(BootStrapInfoProviderElement bootStrapInfo) {

		PANApplianceBootstrapInformationElement bootstrapElement = new PANApplianceBootstrapInformationElement();
		byte [] nullEntry = Base64.encodeBase64(("").getBytes());
		try {
			bootstrapElement.addBootstrapFile("/config/init-cfg.txt",getInitCfg(bootStrapInfo));
			bootstrapElement.addBootstrapFile("/config/bootstrap.xml",getBootstrapXML(bootStrapInfo));
			bootstrapElement.addBootstrapFile("/license/authcodes",getLicense());
			bootstrapElement.addBootstrapFile("/content",nullEntry);
			bootstrapElement.addBootstrapFile("/software",nullEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return bootstrapElement;
	}

	protected byte[] getInitCfg(BootStrapInfoProviderElement bootStrapInfo) {

		// Pass in bootstrap info for device
		// Same info for all devices
		// use vss element to determine device group
		// give me config drive info for device group
		byte[] encoded;
		StringBuilder configString = new StringBuilder();

		configString.append("type=dhcp-client"+System.lineSeparator());
		configString.append("ip-address="+System.lineSeparator());
		configString.append("default-gateway="+System.lineSeparator());
		configString.append("netmask="+System.lineSeparator());
		configString.append("ipv6-address="+System.lineSeparator());
		configString.append("ipv6-default-gateway="+System.lineSeparator());
		configString.append("hostname=" + bootStrapInfo.getName() + System.lineSeparator());
		configString.append("panorama-server="+ this.mc.getIpAddress() + System.lineSeparator());
		configString.append("panorama-server-2="+System.lineSeparator());
		configString.append("tplname="+System.lineSeparator());
		configString.append("dgname="+this.vs.getName()+System.lineSeparator());
		configString.append("dns-primary=8.8.8.8"+System.lineSeparator());
		configString.append("dns-secondary="+System.lineSeparator());
		configString.append("op-command-modes="+System.lineSeparator());
		configString.append("dhcp-send-hostname=yes"+System.lineSeparator());
		configString.append("dhcp-send-client-id=yes"+System.lineSeparator());
		configString.append("dhcp-accept-server-hostname=no"+System.lineSeparator());
		configString.append("dhcp-accept-server-domain=yes"+System.lineSeparator());
		configString.append("vm-auth-key="+vmAuthKey);
		encoded = (configString.toString()).getBytes(StandardCharsets.UTF_8);
		//return Base64.encode(encoded);
		return encoded;
	}

	protected byte[] getLicense(){
		byte[] encoded;
		encoded = (LICENSE_AUTH_CODE.getBytes(StandardCharsets.UTF_8));
		return encoded;
	}

	protected byte[] getBootstrapXML(BootStrapInfoProviderElement bootStrapInfo){
		byte[] encoded;
		String configString;
		configString = "<?xml version=\"1.0\"?>\n" +
				"<config version=\"7.1.0\" urldb=\"paloaltonetworks\">\n" +
				"  <mgt-config>\n" +
				"    <users>\n" +
				"      <entry name=\"admin\">\n" +
				"        <phash>fnRL/G5lXVMug</phash>\n" +
				"        <permissions>\n" +
				"          <role-based>\n" +
				"            <superuser>yes</superuser>\n" +
				"          </role-based>\n" +
				"        </permissions>\n" +
				"      </entry>\n" +
				"    </users>\n" +
				"  </mgt-config>\n" +
				"  <shared>\n" +
				"    <application/>\n" +
				"    <application-group/>\n" +
				"    <service/>\n" +
				"    <service-group/>\n" +
				"    <botnet>\n" +
				"      <configuration>\n" +
				"        <http>\n" +
				"          <dynamic-dns>\n" +
				"            <enabled>yes</enabled>\n" +
				"            <threshold>5</threshold>\n" +
				"          </dynamic-dns>\n" +
				"          <malware-sites>\n" +
				"            <enabled>yes</enabled>\n" +
				"            <threshold>5</threshold>\n" +
				"          </malware-sites>\n" +
				"          <recent-domains>\n" +
				"            <enabled>yes</enabled>\n" +
				"            <threshold>5</threshold>\n" +
				"          </recent-domains>\n" +
				"          <ip-domains>\n" +
				"            <enabled>yes</enabled>\n" +
				"            <threshold>10</threshold>\n" +
				"          </ip-domains>\n" +
				"          <executables-from-unknown-sites>\n" +
				"            <enabled>yes</enabled>\n" +
				"            <threshold>5</threshold>\n" +
				"          </executables-from-unknown-sites>\n" +
				"        </http>\n" +
				"        <other-applications>\n" +
				"          <irc>yes</irc>\n" +
				"        </other-applications>\n" +
				"        <unknown-applications>\n" +
				"          <unknown-tcp>\n" +
				"            <destinations-per-hour>10</destinations-per-hour>\n" +
				"            <sessions-per-hour>10</sessions-per-hour>\n" +
				"            <session-length>\n" +
				"              <maximum-bytes>100</maximum-bytes>\n" +
				"              <minimum-bytes>50</minimum-bytes>\n" +
				"            </session-length>\n" +
				"          </unknown-tcp>\n" +
				"          <unknown-udp>\n" +
				"            <destinations-per-hour>10</destinations-per-hour>\n" +
				"            <sessions-per-hour>10</sessions-per-hour>\n" +
				"            <session-length>\n" +
				"              <maximum-bytes>100</maximum-bytes>\n" +
				"              <minimum-bytes>50</minimum-bytes>\n" +
				"            </session-length>\n" +
				"          </unknown-udp>\n" +
				"        </unknown-applications>\n" +
				"      </configuration>\n" +
				"      <report>\n" +
				"        <topn>100</topn>\n" +
				"        <scheduled>yes</scheduled>\n" +
				"      </report>\n" +
				"    </botnet>\n" +
				"  </shared>\n" +
				"  <devices>\n" +
				"    <entry name=\"localhost.localdomain\">\n" +
				"      <network>\n" +
				"        <interface>\n" +
				"          <ethernet>\n" +
				"            <entry name=\"ethernet1/1\">\n" +
				"              <virtual-wire>\n" +
				"                <lldp>\n" +
				"                  <enable>no</enable>\n" +
				"                </lldp>\n" +
				"              </virtual-wire>\n" +
				"            </entry>\n" +
				"            <entry name=\"ethernet1/2\">\n" +
				"              <virtual-wire>\n" +
				"                <lldp>\n" +
				"                  <enable>no</enable>\n" +
				"                </lldp>\n" +
				"              </virtual-wire>\n" +
				"            </entry>\n" +
				"          </ethernet>\n" +
				"        </interface>\n" +
				"        <profiles>\n" +
				"          <monitor-profile>\n" +
				"            <entry name=\"default\">\n" +
				"              <interval>3</interval>\n" +
				"              <threshold>5</threshold>\n" +
				"              <action>wait-recover</action>\n" +
				"            </entry>\n" +
				"          </monitor-profile>\n" +
				"        </profiles>\n" +
				"        <ike>\n" +
				"          <crypto-profiles>\n" +
				"            <ike-crypto-profiles>\n" +
				"              <entry name=\"default\">\n" +
				"                <encryption>\n" +
				"                  <member>aes-128-cbc</member>\n" +
				"                  <member>3des</member>\n" +
				"                </encryption>\n" +
				"                <hash>\n" +
				"                  <member>sha1</member>\n" +
				"                </hash>\n" +
				"                <dh-group>\n" +
				"                  <member>group2</member>\n" +
				"                </dh-group>\n" +
				"                <lifetime>\n" +
				"                  <hours>8</hours>\n" +
				"                </lifetime>\n" +
				"              </entry>\n" +
				"              <entry name=\"Suite-B-GCM-128\">\n" +
				"                <encryption>\n" +
				"                  <member>aes-128-cbc</member>\n" +
				"                </encryption>\n" +
				"                <hash>\n" +
				"                  <member>sha256</member>\n" +
				"                </hash>\n" +
				"                <dh-group>\n" +
				"                  <member>group19</member>\n" +
				"                </dh-group>\n" +
				"                <lifetime>\n" +
				"                  <hours>8</hours>\n" +
				"                </lifetime>\n" +
				"              </entry>\n" +
				"              <entry name=\"Suite-B-GCM-256\">\n" +
				"                <encryption>\n" +
				"                  <member>aes-256-cbc</member>\n" +
				"                </encryption>\n" +
				"                <hash>\n" +
				"                  <member>sha384</member>\n" +
				"                </hash>\n" +
				"                <dh-group>\n" +
				"                  <member>group20</member>\n" +
				"                </dh-group>\n" +
				"                <lifetime>\n" +
				"                  <hours>8</hours>\n" +
				"                </lifetime>\n" +
				"              </entry>\n" +
				"            </ike-crypto-profiles>\n" +
				"            <ipsec-crypto-profiles>\n" +
				"              <entry name=\"default\">\n" +
				"                <esp>\n" +
				"                  <encryption>\n" +
				"                    <member>aes-128-cbc</member>\n" +
				"                    <member>3des</member>\n" +
				"                  </encryption>\n" +
				"                  <authentication>\n" +
				"                    <member>sha1</member>\n" +
				"                  </authentication>\n" +
				"                </esp>\n" +
				"                <dh-group>group2</dh-group>\n" +
				"                <lifetime>\n" +
				"                  <hours>1</hours>\n" +
				"                </lifetime>\n" +
				"              </entry>\n" +
				"              <entry name=\"Suite-B-GCM-128\">\n" +
				"                <esp>\n" +
				"                  <encryption>\n" +
				"                    <member>aes-128-gcm</member>\n" +
				"                  </encryption>\n" +
				"                  <authentication>\n" +
				"                    <member>none</member>\n" +
				"                  </authentication>\n" +
				"                </esp>\n" +
				"                <dh-group>group19</dh-group>\n" +
				"                <lifetime>\n" +
				"                  <hours>1</hours>\n" +
				"                </lifetime>\n" +
				"              </entry>\n" +
				"              <entry name=\"Suite-B-GCM-256\">\n" +
				"                <esp>\n" +
				"                  <encryption>\n" +
				"                    <member>aes-256-gcm</member>\n" +
				"                  </encryption>\n" +
				"                  <authentication>\n" +
				"                    <member>none</member>\n" +
				"                  </authentication>\n" +
				"                </esp>\n" +
				"                <dh-group>group20</dh-group>\n" +
				"                <lifetime>\n" +
				"                  <hours>1</hours>\n" +
				"                </lifetime>\n" +
				"              </entry>\n" +
				"            </ipsec-crypto-profiles>\n" +
				"            <global-protect-app-crypto-profiles>\n" +
				"              <entry name=\"default\">\n" +
				"                <encryption>\n" +
				"                  <member>aes-128-cbc</member>\n" +
				"                </encryption>\n" +
				"                <authentication>\n" +
				"                  <member>sha1</member>\n" +
				"                </authentication>\n" +
				"              </entry>\n" +
				"            </global-protect-app-crypto-profiles>\n" +
				"          </crypto-profiles>\n" +
				"        </ike>\n" +
				"        <qos>\n" +
				"          <profile>\n" +
				"            <entry name=\"default\">\n" +
				"              <class>\n" +
				"                <entry name=\"class1\">\n" +
				"                  <priority>real-time</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class2\">\n" +
				"                  <priority>high</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class3\">\n" +
				"                  <priority>high</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class4\">\n" +
				"                  <priority>medium</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class5\">\n" +
				"                  <priority>medium</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class6\">\n" +
				"                  <priority>low</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class7\">\n" +
				"                  <priority>low</priority>\n" +
				"                </entry>\n" +
				"                <entry name=\"class8\">\n" +
				"                  <priority>low</priority>\n" +
				"                </entry>\n" +
				"              </class>\n" +
				"            </entry>\n" +
				"          </profile>\n" +
				"        </qos>\n" +
				"        <virtual-router>\n" +
				"          <entry name=\"default\">\n" +
				"            <protocol>\n" +
				"              <bgp>\n" +
				"                <enable>no</enable>\n" +
				"                <dampening-profile>\n" +
				"                  <entry name=\"default\">\n" +
				"                    <cutoff>1.25</cutoff>\n" +
				"                    <reuse>0.5</reuse>\n" +
				"                    <max-hold-time>900</max-hold-time>\n" +
				"                    <decay-half-life-reachable>300</decay-half-life-reachable>\n" +
				"                    <decay-half-life-unreachable>900</decay-half-life-unreachable>\n" +
				"                    <enable>yes</enable>\n" +
				"                  </entry>\n" +
				"                </dampening-profile>\n" +
				"              </bgp>\n" +
				"            </protocol>\n" +
				"          </entry>\n" +
				"        </virtual-router>\n" +
				"        <virtual-wire>\n" +
				"          <entry name=\"OSC\">\n" +
				"            <interface1>ethernet1/1</interface1>\n" +
				"            <interface2>ethernet1/2</interface2>\n" +
				"          </entry>\n" +
				"        </virtual-wire>\n" +
				"      </network>\n" +
				"      <deviceconfig>\n" +
				"        <system>\n" +
				"          <update-server>updates.paloaltonetworks.com</update-server>\n" +
				"          <update-schedule>\n" +
				"            <threats>\n" +
				"              <recurring>\n" +
				"                <weekly>\n" +
				"                  <day-of-week>wednesday</day-of-week>\n" +
				"                  <at>01:02</at>\n" +
				"                  <action>download-only</action>\n" +
				"                </weekly>\n" +
				"              </recurring>\n" +
				"            </threats>\n" +
				"          </update-schedule>\n" +
				"          <timezone>US/Pacific</timezone>\n" +
				"          <service>\n" +
				"            <disable-telnet>yes</disable-telnet>\n" +
				"            <disable-http>yes</disable-http>\n" +
				"          </service>\n" +
				"          <type>\n" +
				"            <dhcp-client>\n" +
				"              <send-hostname>yes</send-hostname>\n" +
				"              <send-client-id>yes</send-client-id>\n" +
				"              <accept-dhcp-hostname>no</accept-dhcp-hostname>\n" +
				"              <accept-dhcp-domain>yes</accept-dhcp-domain>\n" +
				"            </dhcp-client>\n" +
				"          </type>\n" +
				"          <hostname>" + bootStrapInfo.getName() + "</hostname>\n" +
				"          <panorama-server>" + this.mc.getIpAddress() + "</panorama-server>\n" +
				"          <dns-setting>\n" +
				"            <servers>\n" +
				"              <primary>8.8.8.8</primary>\n" +
				"            </servers>\n" +
				"          </dns-setting>\n" +
				"        </system>\n" +
				"        <setting>\n" +
				"          <config>\n" +
				"            <rematch>yes</rematch>\n" +
				"          </config>\n" +
				"          <management>\n" +
				"            <hostname-type-in-syslog>FQDN</hostname-type-in-syslog>\n" +
				"            <initcfg>\n" +
				"              <type>\n" +
				"                <dhcp-client>\n" +
				"                  <send-hostname>yes</send-hostname>\n" +
				"                  <send-client-id>yes</send-client-id>\n" +
				"                  <accept-dhcp-hostname>no</accept-dhcp-hostname>\n" +
				"                  <accept-dhcp-domain>yes</accept-dhcp-domain>\n" +
				"                </dhcp-client>\n" +
				"              </type>\n" +
                "              <hostname>" + bootStrapInfo.getName() + "</hostname>\n" +
                "              <panorama-server>" + this.mc.getIpAddress() + "</panorama-server>\n" +
				"              <dns-primary>8.8.8.8</dns-primary>\n" +
				"              <vm-auth-key>384897810647463</vm-auth-key>\n" +
				"            </initcfg>\n" +
				"          </management>\n" +
				"        </setting>\n" +
				"      </deviceconfig>\n" +
				"      <vsys>\n" +
				"        <entry name=\"vsys1\">\n" +
				"          <application/>\n" +
				"          <application-group/>\n" +
				"          <zone>\n" +
				"            <entry name=\"OSC\">\n" +
				"              <network>\n" +
				"                <virtual-wire>\n" +
				"                  <member>ethernet1/1</member>\n" +
				"                  <member>ethernet1/2</member>\n" +
				"                </virtual-wire>\n" +
				"              </network>\n" +
				"            </entry>\n" +
				"          </zone>\n" +
				"          <service/>\n" +
				"          <service-group/>\n" +
				"          <schedule/>\n" +
				"          <rulebase>\n" +
				"            <security>\n" +
				"              <rules>\n" +
				"                <entry name=\"allow-allow\">\n" +
				"                  <to>\n" +
				"                    <member>any</member>\n" +
				"                  </to>\n" +
				"                  <from>\n" +
				"                    <member>any</member>\n" +
				"                  </from>\n" +
				"                  <source>\n" +
				"                    <member>any</member>\n" +
				"                  </source>\n" +
				"                  <destination>\n" +
				"                    <member>any</member>\n" +
				"                  </destination>\n" +
				"                  <source-user>\n" +
				"                    <member>any</member>\n" +
				"                  </source-user>\n" +
				"                  <category>\n" +
				"                    <member>any</member>\n" +
				"                  </category>\n" +
				"                  <application>\n" +
				"                    <member>any</member>\n" +
				"                  </application>\n" +
				"                  <service>\n" +
				"                    <member>application-default</member>\n" +
				"                  </service>\n" +
				"                  <hip-profiles>\n" +
				"                    <member>any</member>\n" +
				"                  </hip-profiles>\n" +
				"                  <action>allow</action>\n" +
				"                </entry>\n" +
				"              </rules>\n" +
				"            </security>\n" +
				"            <default-security-rules>\n" +
				"              <rules>\n" +
				"                <entry name=\"interzone-default\">\n" +
				"                  <action>allow</action>\n" +
				"                  <log-start>no</log-start>\n" +
				"                  <log-end>no</log-end>\n" +
				"                </entry>\n" +
				"              </rules>\n" +
				"            </default-security-rules>\n" +
				"          </rulebase>\n" +
				"          <import>\n" +
				"            <network>\n" +
				"              <interface>\n" +
				"                <member>ethernet1/1</member>\n" +
				"                <member>ethernet1/2</member>\n" +
				"              </interface>\n" +
				"            </network>\n" +
				"          </import>\n" +
				"        </entry>\n" +
				"      </vsys>\n" +
				"    </entry>\n" +
				"  </devices>\n" +
				"</config>\n" +
				"";

		encoded = (configString.getBytes(StandardCharsets.UTF_8));
		//return Base64.encode(encoded);
		return encoded;
	}
	@Override
	public void close() {


	}


}