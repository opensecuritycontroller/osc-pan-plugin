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
package com.paloaltonetworks.osc.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerSecurityGroupInterfaceElement;

public class SecurityGroupInterfaceListElement extends BaseIdNameObject implements ManagerSecurityGroupInterfaceElement {

    public SecurityGroupInterfaceListElement(String id, String name, String policyId, String tag) {
        super(id, name);
        this.policyId = policyId;
        this.tag = tag;
    }

    private String policyId;
    private String tag;

 
    public String getPolicyId() {
        return this.policyId;
    }

   
    public String getTag() {
        return this.tag;
    }

  
    public String getSecurityGroupInterfaceId() {
        return getId();
    }

}
