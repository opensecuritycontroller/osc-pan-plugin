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

import java.util.HashSet;
import java.util.List;
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
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupApi implements ManagerSecurityGroupApi {

    private static final Logger LOG = LoggerFactory.getLogger(PANManagerSecurityGroupApi.class);

    public static final String SG_TAG_PREFIX = "OSC_SecurityGroup_";

    static String apiKey = null;
    private VirtualSystemElement vs;
    private ApplianceManagerConnectorElement mc;
    private PanoramaApiClient panClient;

    public PANManagerSecurityGroupApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs,
            PanoramaApiClient panClient) {
        this.vs = vs;
        this.mc = mc;
        this.panClient = panClient;

    }

    @Override
    public String createSecurityGroup(String name, String iscId, SecurityGroupMemberListElement memberList)
            throws Exception {
        try {
            LOG.info("Adding security group %s with members %s", name, memberList);
            doUpdateSecurityGroup(name, name, memberList);
        } catch (Exception e) {
            LOG.error("Exception adding security group " + name, e);
            return null;
        }

        return "success";
    }

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
        Set<String> ipsOnMgr = fetchMgrIps(sgId);
        try {
            for (String ip : ipsOnMgr) {
                LOG.info("Deleting address %s", ip);
                this.panClient.deleteAddress(ip);
            }
        } catch (Exception e) {
            LOG.error("Exception deleting security group " + sgId, e);
            throw e;
        }

        this.panClient.deleteSGTag(sgId);
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
        LOG.info("Populating security group %s with members: %s", name, memberList.toString());
        String sgTag = name;

        addSGTagAndCheck(name, sgTag);

        Set<String> rightIps = computeRightIps(memberList);
        Set<String> ipsOnMgr = fetchMgrIps(sgTag);
        Set<String> tmp = new HashSet<>(ipsOnMgr);
        ipsOnMgr.removeAll(rightIps);
        rightIps.removeAll(tmp);

        for (String ip : rightIps) {
            LOG.info("Creating address %s", ip);
            this.panClient.addAddress(ip);
            LOG.info("Adding address %s to security group %s ", ip, name);
            this.panClient.addSGTagToAddress(ip, sgTag);
        }

        for (String ip : ipsOnMgr) {
            LOG.info("Deleting address %s", ip);
            this.panClient.deleteAddress(ip);
        }
    }

    private Set<String> fetchMgrIps(String sgTag) throws Exception {
        List<AddressEntry> addrEntries = this.panClient.fetchAddressesBySGTag(sgTag);
        return addrEntries.stream().map(ae -> ae.getName()).collect(Collectors.toSet());
    }

    private Set<String> computeRightIps(SecurityGroupMemberListElement memberList) {
        Set<String> rightIps = new HashSet<>();

        for (SecurityGroupMemberElement member : memberList.getMembers()) {
            rightIps.addAll(member.getIpAddresses());
        }

        return rightIps;
    }

    private void addSGTagAndCheck(String name, String sgTag) throws Exception {
        try {
            this.panClient.addSGTag(sgTag); // OK if exists
            if (!this.panClient.sgTagExists(sgTag)) {
                throw new Exception("Security group " + name + " not found after add call to panorama!");
            }
        } catch (Exception e) {
            LOG.error("Exception adding security group " + name, e);
            throw e;
        }
    }
}
