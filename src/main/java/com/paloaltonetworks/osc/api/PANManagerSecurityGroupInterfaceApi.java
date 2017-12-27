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

import java.util.List;

import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

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
        String status = this.panClient.addDAGTag(sgiElement.getTag());
        if (status.equals("success")) {
            return sgiElement.getName();
        } else {
            return null;
        }
    }

    @Override
    public void updateSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement) throws Exception {

    }

    @Override
    public void deleteSecurityGroupInterface(String id) throws Exception {
        this.panClient.deleteDAGTag(id);
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

}
