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

import com.intelsecurity.isc.plugin.manager.api.ManagerDomainApi;
import com.intelsecurity.isc.plugin.manager.element.ApplianceManagerConnectorElement;
import com.paloaltonetworks.osc.model.Domain;
import com.paloaltonetworks.osc.model.DomainListElement;


/**
 * This documents "Device Management Apis"
 */
public class PANManagerDomainApi implements ManagerDomainApi  {

	
    public PANManagerDomainApi(ApplianceManagerConnectorElement mc) throws Exception {

    }

    public static PANManagerDomainApi create(ApplianceManagerConnectorElement mc) throws Exception {
        return new PANManagerDomainApi(mc);
    }
    @Override
    public Domain getDomain(String domainId) throws Exception {
        return new Domain("Roo-Domain", "Root-Domain");
    }

    @Override
    public List<DomainListElement> listDomains() throws Exception {
        List<DomainListElement> domainList = new ArrayList<DomainListElement>();
        domainList.add(new DomainListElement("Root-Domain", "Root-Domain"));
        return domainList;
    }
	

}