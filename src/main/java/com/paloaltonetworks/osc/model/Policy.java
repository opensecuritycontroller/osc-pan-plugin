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

import org.osc.sdk.manager.element.ManagerPolicyElement;

public class Policy implements ManagerPolicyElement {

    protected String id;
    protected String name;
    protected String domainId;

    public Policy(String id, String name, String domainId) {
        this.id = id;
        this.name = name;
        this.domainId = domainId;
    }

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDomainId() {
		return this.domainId;
	}

}
