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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entry")
@XmlRootElement(name = "entry")
public class TagEntry {
    public TagEntry() {
    }

    public TagEntry(String color, String comment, String name) {
        this.color = color;
        this.comment = comment;
        this.name = name;
    }

    private String color;
    private String comment;

    @XmlAttribute
    private String name;

    public String getName(){
        return this.name;
    }

    public void setName(String value){
        this.name = value;
    }

    public String getColor(){
        return this.color;
    }
    public void setColor(String value){
        this.color = value;
    }

    public String getComment(){
        return this.comment;
    }

    public void setComment(String value){
        this.comment = value;
    }
}