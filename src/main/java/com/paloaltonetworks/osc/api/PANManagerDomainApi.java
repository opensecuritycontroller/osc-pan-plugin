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

import java.util.Arrays;
import java.util.List;

import org.osc.sdk.manager.api.ManagerDomainApi;

import com.paloaltonetworks.osc.model.PANDomainElement;
import com.paloaltonetworks.osc.model.PANDomainListElement;

/**
 * This documents "Device Management Apis"
 */
public class PANManagerDomainApi implements ManagerDomainApi {

    private static final String DEFAULT_DOMAIN = "Shared";

    @Override
    public PANDomainElement getDomain(String domainId) throws Exception {
        return DEFAULT_DOMAIN.equals(domainId) ?  new PANDomainElement(DEFAULT_DOMAIN, DEFAULT_DOMAIN) : null;
    }

    @Override
    public List<PANDomainListElement> listDomains() throws Exception {
        return Arrays.asList(new PANDomainListElement(DEFAULT_DOMAIN, DEFAULT_DOMAIN));
    }
}
