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

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.osc.model.PolicyListElement;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerPolicyApi implements ManagerPolicyApi {

    private static final Logger log = LoggerFactory.getLogger(PANManagerPolicyApi.class);
    private static final String XPATH_SHARED_TAG = "/config/shared/tag";

    static String apiKey = null;
    private PanoramaApiClient panClient;

    public PANManagerPolicyApi(PanoramaApiClient panClient) {
        log.info("Creating new PANManagerPolicy api");
        this.panClient = panClient;
    }

    @Override
    public PolicyListElement getPolicy(String policyId, String domainId) throws Exception {
        if (this.panClient.tagExists(policyId, XPATH_SHARED_TAG)) {
            return new PolicyListElement(policyId, policyId, domainId);
        }

        return null;
    }

    @Override
    public List<PolicyListElement> getPolicyList(String domainId) throws Exception {
        return this.panClient.getTagEntries(XPATH_SHARED_TAG)
                             .stream()
                             .filter(t -> t != null)
                             .map(t -> new PolicyListElement(t.getName(), t.getName(), domainId))
                             .collect(toList());
    }
}
