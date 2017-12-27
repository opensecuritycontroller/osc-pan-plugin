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
@XmlRootElement(name="response")
public class DeviceGroupResponse implements PANResponse {

    @XmlAttribute(name = "status")
    private String status;

    @XmlElement(name="result")
    private DeviceGroups deviceGroups;

    @Override
    public String getStatus(){
        return this.status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public DeviceGroups getDeviceGroups(){
        return this.deviceGroups;
    }

    public void setDeviceGroups(DeviceGroups value){
        this.deviceGroups = value;
    }

    @Override
    public String getCode() {
        throw new UnsupportedOperationException("getCode not implemented for class "+ getClass());
    }
}
