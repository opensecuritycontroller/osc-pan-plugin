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

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.slf4j.Logger;

import com.paloaltonetworks.osc.model.PolicyListElement;
import com.paloaltonetworks.panorama.api.methods.ShowOperations;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerPolicyApi implements ManagerPolicyApi {

    private static final Logger log = LoggerFactory.getLogger(PANManagerPolicyApi.class);
    static String apiKey = null;
    private VirtualSystemElement vs;
    private ApplianceManagerConnectorElement mc;
    private ShowOperations showOperations;

    private static ArrayList<PolicyListElement> policyList = new ArrayList<>();
    static {
        // Add domain if applicable
        policyList.add(new PolicyListElement("Platinum", "Platinum", "Root-Domain"));
        policyList.add(new PolicyListElement("Gold", "Gold", "Root-Domain"));
        policyList.add(new PolicyListElement("Silver", "Silver", "Root-Domain"));
        policyList.add(new PolicyListElement("Bronze", "Bronze", "Root-Domain"));
    }

    public PANManagerPolicyApi(ApplianceManagerConnectorElement mc, ShowOperations showOperations) {
        log.info("Creating new PANManagerPolicy api");
        this.mc = mc;
        log.info("new show operaitons in Policy");
        this.showOperations = showOperations;

    }

    @Override
    public PolicyListElement getPolicy(String policyId, String domainId) throws Exception {
        return policyList.get(Integer.valueOf(policyId));
    }

    @Override
    public List<PolicyListElement> getPolicyList(String domainId) throws Exception {
        return policyList;
    }
}
