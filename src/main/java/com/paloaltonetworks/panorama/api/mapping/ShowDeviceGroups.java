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
package com.paloaltonetworks.panorama.api.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "entry")
public class ShowDeviceGroups {
	
	@XmlElement(name="devices")
	ShowBaseEntry baseEntry;
	
	@XmlElement(name="shared-policy-md5sum")
	private String sharedPolicy;
	
	//@XmlPath("entry[@name='name']/text()")
	//private String name;
	
	@XmlAttribute(name="name")
	private String name;
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String value){
		this.name = value;
	}
	
	public String getSharedPolicy(){
		return sharedPolicy;
	}
	public void setSharedPolicy(String value){
		this.sharedPolicy = value;
	}
	
	public ShowBaseEntry getShowBaseEntry(){
		return this.baseEntry;
	}
	
	public void setShowBaseEntry(ShowBaseEntry value){
		this.baseEntry = value;
	}
}
