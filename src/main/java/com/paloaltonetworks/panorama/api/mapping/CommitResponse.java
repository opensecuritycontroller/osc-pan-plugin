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


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="response")
public class CommitResponse {

	
	
	@XmlAttribute(name = "status")
	private String status;
	
	public String getStatus(){
		return this.status;
	}
	
	public void setStatus(String value) {
		this.status = value;
	}
	
	@XmlAttribute(name = "code")
	private String code;
	
	public String getCode(){
		return this.code;
	}
	
	public void setCode(String value) {
		this.code = value;
	}
	
	@XmlElement(name="result")
	private CommitResult commitResult;
	
	public CommitResult getCommitResult(){
		return commitResult;
	}
	
	public void setCommitResult(CommitResult value){
		this.commitResult = value;
	}
	
	
	
	
	
}
