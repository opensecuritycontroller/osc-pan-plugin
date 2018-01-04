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

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.osc.model.PANPolicyListElement;
import com.paloaltonetworks.osc.model.PANSecurityGroupInterfaceElement;
import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;
import com.paloaltonetworks.utils.TagToSGIdUtil;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PANManagerSecurityGroupInterfaceApi.class);

    private static final Character IDSTRING_SEPARATOR = '_';

    private VirtualSystemElement vs;
    private PanoramaApiClient panClient;

    private String devGroup;

    public PANManagerSecurityGroupInterfaceApi(VirtualSystemElement vs,
            PanoramaApiClient panClient) {
        this.vs = vs;
        this.panClient = panClient;
        this.devGroup = vs.getName();
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
        String sgMgrId = extractSGId(sgInterfaceId);

        List<AddressEntry> ipsOnMgr = this.panClient.fetchAddressesWithTag(sgMgrId, this.devGroup);

        if (ipsOnMgr.isEmpty()) {
            LOG.info("Security group {} on VS {} has no addresses. Delete SGi is a no-op.", sgMgrId, this.vs.getName());
        }

        for (AddressEntry entry : ipsOnMgr) {
            List<String> tagToUnbind = entry.getTagNames();
            tagToUnbind.remove(sgMgrId); // Keep sgMgrId bound
            removeTagsFromAllAddresses(entry, tagToUnbind);
        }

        this.panClient.configCommit();
    }

    @Override
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String sgInfcMgrId) throws Exception {
        String sgMgrId = extractSGId(sgInfcMgrId);
        PANSecurityGroupInterfaceElement retVal = new PANSecurityGroupInterfaceElement(sgInfcMgrId, sgInfcMgrId,
                                                            new HashSet<>(), sgInfcMgrId, sgMgrId);

        List<AddressEntry> addresses = this.panClient.getAddressEntries(this.devGroup)
                                                    .stream()
                                                    .filter(ae -> ae != null && ae.getTagNames() != null)
                                                    .filter(ae -> ae.getTagNames().contains(sgMgrId))
                                                    .collect(toList());
        if (addresses.isEmpty()) {
            return null;
        }

        for (AddressEntry ae : addresses) {
            Set<ManagerPolicyElement> currElements = extractPolicyElements(ae);
            retVal.addManagerPolicyElements(currElements);
        }

        return retVal;
    }

    @Override
    public String findSecurityGroupInterfaceByName(String name) throws Exception {
        return null;
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        Map<String, ManagerSecurityGroupInterfaceElement> results = new HashMap<>();

        for (AddressEntry ae : this.panClient.getAddressEntries(this.devGroup)) {

            if (ae == null || ae.getTagNames() == null) {
                continue;
            }

            String sgMgrId = ae.getTagNames().stream().filter(TagToSGIdUtil::isSGTag).findFirst().orElse(null);

            if (sgMgrId != null) {
                String sgInfcMgrId = combineToSGIntefaceId(sgMgrId, this.vs.getName());
                PANSecurityGroupInterfaceElement res = (PANSecurityGroupInterfaceElement) results.get(sgInfcMgrId);
                if (res == null) {
                    res = new PANSecurityGroupInterfaceElement(sgInfcMgrId, sgInfcMgrId,
                                                               new HashSet<>(), sgInfcMgrId, sgMgrId);
                    results.put(sgInfcMgrId, res);
                }

                Set<ManagerPolicyElement> currElements = extractPolicyElements(ae);
                res.addManagerPolicyElements(currElements);
            } else {
                LOG.error("Address object {} detected under {} with no security group", ae.getName(), this.devGroup);
            }
        }

        return new ArrayList<>(results.values());
    }

    @Override
    public void close() {

    }

    private String doUpdateSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement)
            throws Exception {
        String sgMgrId = sgiElement.getManagerSecurityGroupId();
        String sgInterfaceId = combineToSGIntefaceId(sgMgrId, this.vs.getName());

        LOG.info("Creating SGI {} for security group {}.", sgInterfaceId, sgMgrId);

        List<AddressEntry> ipsOnMgr = this.panClient.fetchAddressesWithTag(sgMgrId, this.devGroup);

        if (ipsOnMgr.isEmpty()) {
            LOG.info("Security group {} on VS {} has no addresses. Cannot conform policies for SGI.", sgInterfaceId, this.vs.getName());
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
                tags.remove(sgMgrId);

                Set<String> tmp = new HashSet<>(policyTags);
                tmp.removeAll(tags);
                tags.removeAll(policyTags);

                removeTagsFromAllAddresses(entry, tags);
                addTagsToAllAddresses(entry, tmp);
            }
        } catch (Exception e) {
            LOG.error(String.format("Exception conforming SGI for security group %s on VS %d .", sgInterfaceId, this.vs.getName()), e);
            throw e;
        }
        this.panClient.configCommit();
        return sgInterfaceId;
    }

    private void removeTagsFromAllAddresses(AddressEntry entry, Collection<String> tags) throws Exception {
        for (String tag : tags) {
            this.panClient.removeTagFromAddress(entry.getName(), tag, this.devGroup);
        }
    }

    private void addTagsToAllAddresses(AddressEntry entry, Collection<String> tags) throws Exception {
        for (String tag : tags) {
            this.panClient.addTagToAddress(entry.getName(), tag, this.devGroup);
        }
    }

    /**
     * The manager-side security group interface id is a string tag. It is a
     * combination of Security Group Id and the Virtual System name.
     * This method extracts the sgId part.
     * <p/>
     * That simply splits away the first part of the interface id. For example: <p/>
     *
     * {@code "OSCSecurityGroup_54321_MyVirtSys" -> "OSCSecurityGroup_54321"}
     *
     * <p/>
     * Virtual system names cannot contain underscores.
     *
     * @param sgInterfaceId
     * @return Security Group Id on the appliance manager (Panorama).
     */
    private static String extractSGId(String sgInterfaceId) {
        // Underscores are not allowed in DA names and hence in VS names.
        @SuppressWarnings("boxing")
        int i = sgInterfaceId.lastIndexOf(IDSTRING_SEPARATOR);
        String withoutSGPrefix = sgInterfaceId.substring(0, i);
        return withoutSGPrefix.substring(0, i);
    }

    /**
     * The manager-side security group interface id is a string tag. It is a
     * combination of Security Group Id and the Virtual System name.
     * This method combines the two arguments into that interface id.
     * <p/>
     * That is just a concatenation with a separator. For example: <p/>
     *
     * {@code ("OSCSecurityGroup_54321", "MyVirtSys") -> "OSCSecurityGroup_54321_MyVirtSys"}
     *
     * <p/>
     * Virtual system names cannot contain underscores.
     *
     * @param sgId
     * @param vsName
     * @return Security InterfaceGroup Id on the appliance manager (Panorama).
     */
    private static String combineToSGIntefaceId(String sgId, String vsName) {
        return sgId + IDSTRING_SEPARATOR + vsName;
    }

    /**
     * @param ae
     * @return Policy list element objects for each
     */
    private Set<ManagerPolicyElement> extractPolicyElements(AddressEntry ae) {
        return ae.getTagNames().stream()
                .filter(s -> !TagToSGIdUtil.isSGTag(s))
                .map(s -> new PANPolicyListElement(s, s, ""))
                .collect(Collectors.toSet());
    }
}
