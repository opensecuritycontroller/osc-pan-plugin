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

public class SecurityGroupInterfaceListElement extends BaseIdNameObject
		implements ManagerSecurityGroupInterfaceElement {

	public SecurityGroupInterfaceListElement(String id, String name, Set<Policy> policies, String tag,
			String managerSecurityGroupId) {
		super(id, name);
		this.policies = policies;
		this.tag = tag;
		this.managerSecurityGroupId = managerSecurityGroupId;
	}

	private Set<Policy> policies;
	private String tag;
	private String managerSecurityGroupId;

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
		Set<ManagerPolicyElement> managerPolicyElements = new HashSet<>();
		for (Policy policy : this.policies) {
			managerPolicyElements.add(policy);
		}
		return managerPolicyElements;
	}

}
