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

import java.util.ArrayList;
import java.util.List;

import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.SecurityGroupMemberElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupApi implements ManagerSecurityGroupApi {

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
        /*
         * Check if tag exists - if not create
         */
        boolean tagExists;
        String status = null;
        String pan_serial = null;

        tagExists = this.panClient.policyTagExists(name);
        if (!tagExists) {
            status = this.panClient.addDAGTag(name);
            if (!status.equals("success")) {
                return null;
            }
        }

        List<String> ipList = new ArrayList<>();

        for (SecurityGroupMemberElement member : memberList.getMembers()) {
            ipList.addAll(member.getIpAddresses());
        }
        /*
         * Add TAG and IP address
         */
        List<String> pan_serialList = this.panClient.showDevices();

        if (pan_serialList != null && pan_serialList.size() > 0) {
            status = this.panClient.addDAG(name, ipList);
        }

        if (!"success".equals(status)) {
            return null;
        }

        return name;

    }

    @Override
    public void updateSecurityGroup(String sgId, String name, SecurityGroupMemberListElement memberList)
            throws Exception {

    }

    @Override
    public void deleteSecurityGroup(String sgId) throws Exception {
        this.panClient.deleteDAGTag(sgId);

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
}
