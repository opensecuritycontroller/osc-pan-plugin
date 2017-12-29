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

import static com.paloaltonetworks.utils.SecurityGroupIntefaceIdUtil.getSecurityGroupId;
import static com.paloaltonetworks.utils.SecurityGroupIntefaceIdUtil.getSecurityGroupIntefaceId;
import static com.paloaltonetworks.utils.TagToSGIdUtil.getSecurityGroupTag;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;
import com.paloaltonetworks.utils.TagToSGIdUtil;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PANManagerSecurityGroupInterfaceApi.class);

    static String apiKey = null;
    private VirtualSystemElement vs;
    private ApplianceManagerConnectorElement mc;
    private PanoramaApiClient panClient;

    public PANManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs,
            PanoramaApiClient panClient) {
        this.vs = vs;
        this.mc = mc;
        this.panClient = panClient;
    }

    @Override
    public String createSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement) throws Exception {
        return doUpdateSecurityGroupInterface(sgiElement);
    }

    @Override
    public void updateSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement) throws Exception {
        doUpdateSecurityGroupInterface(sgiElement);
    }

    @Override
    public void deleteSecurityGroupInterface(String id) throws Exception {
        String sgId = getSecurityGroupId(id);
        String sgTag = getSecurityGroupTag(id);
        String status = "success";

        List<AddressEntry> ipsOnMgr = this.panClient.fetchAddressesBySGTag(sgTag);

        if (ipsOnMgr.isEmpty()) {
            LOG.info("Security group %s on VS %d has no addresses. Delete SGi is a no-op.", sgId, this.vs.getId());
        }

        for (AddressEntry entry : ipsOnMgr) {
            List<String> tags = entry.getTagNames();
            tags.remove(sgTag); // only policy tags left!
            try {
                unbindAll(entry, tags);
            } catch (Exception e) {
                LOG.error("Exception conforming address object " + entry.getName(), e);
                status = "error";
            }
        }

        if (!"success".equals(status)) {
            throw new Exception("Errors encountered deleting the security group interface " + id);
        }
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

    private String doUpdateSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement)
            throws Exception {
        String sgMgrId = sgiElement.getManagerSecurityGroupId();
        String sgiId = getSecurityGroupIntefaceId(sgMgrId, this.vs.getId().toString());
        String sgTag = TagToSGIdUtil.getSecurityGroupTag(sgMgrId);

        List<AddressEntry> ipsOnMgr = this.panClient.fetchAddressesBySGTag(sgTag);

        if (ipsOnMgr.isEmpty()) {
            LOG.info("Security group %s on VS %d has no addresses. Cannot conform policies for SGI.", sgiId, this.vs.getId());
            return null;
        }

        Set<String> policyTags = emptySet();
        if (sgiElement.getManagerPolicyElements() != null) {
            policyTags = sgiElement.getManagerPolicyElements().stream().map(e -> e.getName()).collect(toSet());
        }

        try {
            for (AddressEntry entry : ipsOnMgr) {
                List<String> tags = entry.getTagNames();
                tags.remove(sgTag);

                Set<String> tmp = new HashSet<>(policyTags);
                tmp.removeAll(tags);
                tags.removeAll(policyTags);

                unbindAll(entry, tags);
                bindAll(entry, tmp);
            }
        } catch (Exception e) {
            LOG.error(String.format("Exception conforming SGI for security group %s on VS %d .", sgiId, this.vs.getId()), e);
            throw e;
        }

        return sgiId;
    }

    private void unbindAll(AddressEntry entry, Collection<String> tags) throws Exception {
        for (String tag : tags) {
            this.panClient.unbindPolicyTagFromAddress(entry.getName(), tag);
        }
    }

    private void bindAll(AddressEntry entry, Collection<String> tags) throws Exception {
        for (String tag : tags) {
            this.panClient.bindPolicyTagToAddress(entry.getName(), tag);
        }
    }
}
