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

import java.util.HashSet;
import java.util.Set;

import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;

public class PANSecurityGroupInterfaceListElement extends BaseIdNameObject
implements ManagerSecurityGroupInterfaceElement {

    private Set<ManagerPolicyElement> policies;
    private String tag;
    private String managerSecurityGroupId;

    public PANSecurityGroupInterfaceListElement(String id, String name, Set<ManagerPolicyElement> policies, String tag,
            String managerSecurityGroupId) {
        super(id, name);
        this.policies = policies;
        this.tag = tag;
        this.managerSecurityGroupId = managerSecurityGroupId;
    }


    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getSecurityGroupInterfaceId() {
        return getId();
    }

    @Override
    public String getSecurityGroupId() {
        return this.managerSecurityGroupId;
    }

    @Override
    public Set<ManagerPolicyElement> getManagerPolicyElements() {
        return new HashSet<>(this.policies);
    }

    public void addManagerPolicyElements(Set<ManagerPolicyElement> policies) {
        if (policies != null) {
            this.policies.addAll(policies);
        }
    }
}
