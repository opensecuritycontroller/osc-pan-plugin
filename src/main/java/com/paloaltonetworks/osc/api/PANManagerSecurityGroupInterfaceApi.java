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

import static com.paloaltonetworks.utils.TagToSGIdUtil.securityGroupTag;
import static com.paloaltonetworks.utils.VsToDevGroupNameUtil.devGroupName;
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

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PANManagerSecurityGroupInterfaceApi.class);

    private static final Character IDSTRING_SEPARATOR = '_';

    private VirtualSystemElement vs;
    private ApplianceManagerConnectorElement mc;
    private PanoramaApiClient panClient;

    private String devGroup;

    public PANManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs,
            PanoramaApiClient panClient) {
        this.vs = vs;
        this.mc = mc;
        this.panClient = panClient;
        this.devGroup = devGroupName(vs.getId());
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
    public void deleteSecurityGroupInterface(String sgInterfaceId) throws Exception {
        String sgId = extractSGId(sgInterfaceId);
        String sgTag = securityGroupTag(sgId);

        List<AddressEntry> ipsOnMgr = this.panClient.fetchAddressesWithTag(sgTag, this.devGroup);

        if (ipsOnMgr.isEmpty()) {
            LOG.info("Security group {} on VS {} has no addresses. Delete SGi is a no-op.", sgId, this.vs.getId());
        }

        for (AddressEntry entry : ipsOnMgr) {
            List<String> tags = entry.getTagNames();
            tags.remove(sgTag); // only policy tags left!
            unbindAll(entry, tags);
        }

        this.panClient.configCommit();
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
        String sgInterfaceId = combineToSGIntefaceId(sgMgrId, this.vs.getId().toString());
        String sgTag = securityGroupTag(sgMgrId);

        List<AddressEntry> ipsOnMgr = this.panClient.fetchAddressesWithTag(sgTag, this.devGroup);

        if (ipsOnMgr.isEmpty()) {
            LOG.info("Security group {} on VS {} has no addresses. Cannot conform policies for SGI.", sgInterfaceId, this.vs.getId());
            return null;
        }

        Set<String> policyTags = emptySet();
        if (sgiElement.getManagerPolicyElements() != null) {
            policyTags = sgiElement.getManagerPolicyElements().stream().filter(e -> e != null)
                                   .map(e -> e.getName()).collect(toSet());
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
            LOG.error(String.format("Exception conforming SGI for security group %s on VS %d .", sgInterfaceId, this.vs.getId()), e);
            throw e;
        }
        this.panClient.configCommit();
        return sgInterfaceId;
    }

    private void unbindAll(AddressEntry entry, Collection<String> tags) throws Exception {
        for (String tag : tags) {
            this.panClient.removeTagFromAddress(entry.getName(), tag, this.devGroup);
        }
    }

    private void bindAll(AddressEntry entry, Collection<String> tags) throws Exception {
        for (String tag : tags) {
            this.panClient.addTagToAddress(entry.getName(), tag, this.devGroup);
        }
    }

    private static String extractSGId(String sgInterfaceId) {
        @SuppressWarnings("boxing")
        int i = sgInterfaceId.indexOf(IDSTRING_SEPARATOR);
        return sgInterfaceId.substring(0, i);
    }

    private static String combineToSGIntefaceId(String sgId, String vsId) {
        return sgId + IDSTRING_SEPARATOR + vsId;
    }
}
