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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.SecurityGroupMemberElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.osc.model.PANSecurityGroupElement;
import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.mapping.GetAddressResponse;
import com.paloaltonetworks.panorama.api.mapping.GetTagResponse;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;
import com.paloaltonetworks.utils.QuickXmlizerUtil;
import com.paloaltonetworks.utils.TagToSGIdUtil;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupApi implements ManagerSecurityGroupApi {

    private static final Logger LOG = LoggerFactory.getLogger(PANManagerSecurityGroupApi.class);

    private PanoramaApiClient panClient;

    private String addrXpath;
    private String tagXpath;
    private String devGroup;

    public PANManagerSecurityGroupApi(VirtualSystemElement vs,
            PanoramaApiClient panClient) {
        this.panClient = panClient;

        this.devGroup = vs.getName();
        this.addrXpath = String.format(PanoramaApiClient.XPATH_DEVGROUP_TEMPL + "/address", this.devGroup);
        this.tagXpath = String.format(PanoramaApiClient.XPATH_DEVGROUP_TEMPL + "/tag", this.devGroup);
    }

    @Override
    public String createSecurityGroup(String name, String sgId, SecurityGroupMemberListElement memberList)
            throws Exception {
        try {
            LOG.info("Adding security group {} with members {}", name, memberList);
            String sgMgrId = TagToSGIdUtil.securityGroupTag(sgId);
            return doUpdateSecurityGroup(sgMgrId, name, memberList);
        } catch (Exception e) {
            LOG.error("Exception adding security group " + name, e);
            throw e;
        }
    }

    @Override
    public void updateSecurityGroup(String sgMgrId, String name, SecurityGroupMemberListElement memberList)
            throws Exception {
        try {
            doUpdateSecurityGroup(sgMgrId, name, memberList);
        } catch (Exception e) {
            LOG.error("Exception updating security group " + sgMgrId, e);
            throw e;
        }
    }

    @Override
    public void deleteSecurityGroup(String sgMgrId) throws Exception {
        LOG.info("Deleting security group {} ", sgMgrId);
        Set<String> ipsOnMgr = fetchMgrIpsByTag(sgMgrId);
        try {
            for (String ip : ipsOnMgr) {
                deleteAddress(ip);
            }
        } catch (Exception e) {
            LOG.error("Exception deleting security group " + sgMgrId, e);
            throw e;
        }

        LOG.info("Deleting Panorama tag {}", sgMgrId);
        deleteSGTag(sgMgrId);
        this.panClient.configCommit();
    }

    @Override
    public List<? extends ManagerSecurityGroupElement> getSecurityGroupList() throws Exception {
        return this.panClient.getAddressEntries(this.devGroup).stream()
                        .map(ae -> ae.getTagNames())
                        .reduce(new ArrayList<String>(), (c1, c2) -> {c1.addAll(c2); return c1;})
                        .stream()
                        .filter(TagToSGIdUtil::isSGTag)
                        .map(s -> new PANSecurityGroupElement(s, s))
                        .collect(Collectors.toList());
    }

    @Override
    public ManagerSecurityGroupElement getSecurityGroupById(String sgMgrId) throws Exception {
        if(sgMgrId == null) {
            throw new IllegalArgumentException("Null Id is not allowed!");
        }

        return getSecurityGroupList().stream().filter(sg -> sgMgrId.equals(sg.getSGId())).findAny().get();
    }

    @Override
    public void close() {
    }

    private String doUpdateSecurityGroup(String sgMgrId, String name, SecurityGroupMemberListElement memberList)
            throws Exception {
        LOG.info("Populating security group {} with members: {}", name, memberList.toString());

        addSGTag(sgMgrId);
        Set<String> ipsToSet = computeIps(memberList);
        Set<String> ipsOnMgr = fetchMgrIpsByTag(sgMgrId);
        Set<String> tmp = new HashSet<>(ipsOnMgr);
        ipsOnMgr.removeAll(ipsToSet);
        ipsToSet.removeAll(tmp);

        for (String ip : ipsToSet) {
            addAddress(ip);
            this.panClient.addTagToAddress(ip, sgMgrId, this.devGroup);
        }

        for (String ip : ipsOnMgr) {
            this.panClient.removeTagFromAddress(ip, sgMgrId, this.devGroup);
            deleteAddress(ip);
        }

        this.panClient.configCommit();
        return sgMgrId;
    }

    private Set<String> fetchMgrIpsByTag(String sgTag) throws Exception {
        return this.panClient
                .fetchAddressesWithTag(sgTag, this.devGroup).stream()
                .map(ae -> ae.getName())
                .collect(Collectors.toSet());
    }

    private Set<String> computeIps(SecurityGroupMemberListElement memberList) {
        Set<String> rightIps = new HashSet<>();

        for (SecurityGroupMemberElement member : memberList.getMembers()) {
            rightIps.addAll(member.getIpAddresses());
        }

        return rightIps;
    }

    private void addAddress(String ip) throws Exception {
        LOG.info("Creating address {}", ip);
        AddressEntry address = new AddressEntry(ip, ip, "Address added and managed by OSC", emptyList());
        String element = QuickXmlizerUtil.xmlString(address);
        Map<String, String> queryStrings = this.panClient.makeSetConfigRequestParams(this.addrXpath, element, null);
        this.panClient.getRequest(queryStrings, GetAddressResponse.class);
    }

    private void deleteAddress(String ip) throws Exception {
        LOG.info("Deleting address {}", ip);
        // After some experimentation, this is how delete works with addresses.
        Map<String, String> queryStrings = this.panClient.makeDeleteConfigRequestParams(this.addrXpath
                + "/entry[ @name=\""+ ip + "\"]", null, null);
        this.panClient.getRequest(queryStrings, GetAddressResponse.class);
    }

    private void addSGTag(String sgTag) throws Exception {
        LOG.info("Adding tag {} under xpath {}", sgTag, this.tagXpath);
        String element = PanoramaApiClient.makeEntryElement(sgTag, "OSC Security group -- autocreated tag", null, null);
        Map<String, String> queryStrings =  this.panClient.makeSetConfigRequestParams(this.tagXpath, element, null);
        this.panClient.getRequest(queryStrings, GetTagResponse.class);
    }

    private void deleteSGTag(String sgTag) throws Exception {
        LOG.info("Deleting tag {} under xpath {}", sgTag, this.tagXpath);
        String element = PanoramaApiClient.makeEntryElement(sgTag);
        Map<String, String> queryStrings = this.panClient.makeDeleteConfigRequestParams(this.tagXpath, element, null);
        this.panClient.getRequest(queryStrings, GetTagResponse.class);
    }
}
