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

import static com.paloaltonetworks.panorama.api.methods.PanoramaApiClient.makeEntryElement;
import static com.paloaltonetworks.utils.TagToSGIdUtil.securityGroupTag;
import static com.paloaltonetworks.utils.VsToDevGroupNameUtil.devGroupName;
import static java.util.Collections.emptyList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.SecurityGroupMemberElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.mapping.GetAddressResponse;
import com.paloaltonetworks.panorama.api.mapping.GetTagResponse;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;
import com.paloaltonetworks.utils.QuickXmlizerUtil;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupApi implements ManagerSecurityGroupApi {

    private static final Logger LOG = LoggerFactory.getLogger(PANManagerSecurityGroupApi.class);

    public static final String SG_TAG_PREFIX = "OSC_SecurityGroup_";

    private VirtualSystemElement vs;
    private ApplianceManagerConnectorElement mc;
    private PanoramaApiClient panClient;

    private String addrXpath;
    private String tagXpath;
    private String devGroup;

    public PANManagerSecurityGroupApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs,
            PanoramaApiClient panClient) {
        this.vs = vs;
        this.mc = mc;
        this.panClient = panClient;

        this.devGroup = devGroupName(vs.getId());
        this.addrXpath = String.format(PanoramaApiClient.XPATH_DEVGROUP_TEMPL + "/address", this.devGroup);
        this.tagXpath = String.format(PanoramaApiClient.XPATH_DEVGROUP_TEMPL + "/tag", this.devGroup);
    }

    /**
    *
    * Note that the order of arguments is reversed: {@code updateSecurityGroup} has {@code name} as a first argument
    * and {@code id} as a second.
    *
    * @see ManagerSecurityGroupApi
    *
    */
    @Override
    public String createSecurityGroup(String name, String sgId, SecurityGroupMemberListElement memberList)
            throws Exception {
        try {
            LOG.info("Adding security group {} with members {}", name, memberList);
            doUpdateSecurityGroup(sgId, name, memberList);
        } catch (Exception e) {
            LOG.error("Exception adding security group " + name, e);
            return null;
        }

        return sgId;
    }

    /**
     *
     * Note that the order of arguments is reversed: {@code createSecurityGroup} has {@code name} as a first argument
     * and {@code id} as a second.
     *
     * @see ManagerSecurityGroupApi
     *
     */
    @Override
    public void updateSecurityGroup(String sgId, String name, SecurityGroupMemberListElement memberList)
            throws Exception {
        try {
            doUpdateSecurityGroup(sgId, name, memberList);
        } catch (Exception e) {
            LOG.error("Exception updating security group " + sgId, e);
        }
    }

    @Override
    public void deleteSecurityGroup(String sgId) throws Exception {
        String sgTag = securityGroupTag(sgId);
        Set<String> ipsOnMgr = fetchMgrIpsByTag(sgTag);
        try {
            for (String ip : ipsOnMgr) {
                LOG.info("Deleting address {}", ip);
                deleteAddress(ip);
            }
        } catch (Exception e) {
            LOG.error("Exception deleting security group " + sgId, e);
            throw e;
        }

        deleteSGTag(sgId);
        this.panClient.configCommit();
    }

    @Override
    public List<? extends ManagerSecurityGroupElement> getSecurityGroupList() throws Exception {
        return null;
    }

    @Override
    public ManagerSecurityGroupElement getSecurityGroupById(String sgId) throws Exception {
        return null;
    }

    @Override
    public void close() {
    }

    private void doUpdateSecurityGroup(String sgId, String name, SecurityGroupMemberListElement memberList)
            throws Exception {
        LOG.info("Populating security group {} with members: {}", name, memberList.toString());
        String sgTag = securityGroupTag(sgId);

        addSGTagAndCheck(sgTag);

        Set<String> ipsToSet = computeIps(memberList);
        Set<String> ipsOnMgr = fetchMgrIpsByTag(sgTag);
        Set<String> tmp = new HashSet<>(ipsOnMgr);
        ipsOnMgr.removeAll(ipsToSet);
        ipsToSet.removeAll(tmp);

        for (String ip : ipsToSet) {
            LOG.info("Creating address {}", ip);
            addAddress(ip);
            LOG.info("Adding address {} to security group {} ", ip, name);
            this.panClient.addTagToAddress(ip, sgTag, this.devGroup);
        }

        for (String ip : ipsOnMgr) {
            this.panClient.removeTagFromAddress(ip, sgTag, this.devGroup);
            LOG.info("Deleting address {}", ip);
            deleteAddress(ip);
        }

        this.panClient.configCommit();
    }

    private Set<String> fetchMgrIpsByTag(String sgTag) throws Exception {
        return this.panClient
                .fetchAddressesWithTag(sgTag, this.devGroup).stream().map(ae -> ae.getName())
                .collect(Collectors.toSet());
    }

    private Set<String> computeIps(SecurityGroupMemberListElement memberList) {
        Set<String> rightIps = new HashSet<>();

        for (SecurityGroupMemberElement member : memberList.getMembers()) {
            rightIps.addAll(member.getIpAddresses());
        }

        return rightIps;
    }

    private void addSGTagAndCheck(String sgTag) throws Exception {
        try {
            LOG.info("Adding tag {} under xpath {}", sgTag, this.tagXpath);
            addSGTag(sgTag); // OK if exists
        } catch (Exception e) {
            LOG.error("Exception adding security group " + sgTag, e);
            throw e;
        }

        if (!this.panClient.tagExists(sgTag, this.tagXpath)) {
            throw new Exception("Security group " + sgTag + " not found after add call to panorama!");
        }

    }

    private void addAddress(String ip) throws Exception {
        AddressEntry address = new AddressEntry(ip, ip, "Address added and managed by OSC", emptyList());
        String element = QuickXmlizerUtil.xmlString(address);
        Map<String, String> queryStrings = this.panClient.makeSetConfigRequestParams(this.addrXpath, element, null);
        this.panClient.getRequest(queryStrings, GetAddressResponse.class);
    }

    private void deleteAddress(String ip) throws Exception {
        // After some experimentation, this is how delete works with addresses.
        Map<String, String> queryStrings = this.panClient.makeDeleteConfigRequestParams(this.addrXpath
                + "/entry[ @name=\""+ ip + "\"]", null, null);
        this.panClient.getRequest(queryStrings, GetAddressResponse.class);
    }

    private void addSGTag(String sgTag) throws Exception {
        String element = makeEntryElement(sgTag, "OSC Security group -- autocreated tag", null, null);
        Map<String, String> queryStrings =  this.panClient.makeSetConfigRequestParams(this.tagXpath, element, null);
        this.panClient.getRequest(queryStrings, GetTagResponse.class);
    }

    private void deleteSGTag(String sgTag) throws Exception {
        String element = makeEntryElement(sgTag);
        Map<String, String> queryStrings = this.panClient.makeDeleteConfigRequestParams(this.tagXpath, element, null);
        this.panClient.getRequest(queryStrings, GetTagResponse.class);
    }
}
