package com.paloaltonetworks.panorama.api.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entry")
@XmlRootElement(name = "entry")
public class AddressEntry {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "ip-netmask")
    private String netmask;

    @XmlElementWrapper(name = "tag")
    @XmlElement(name = "member", type = String.class)
    private List<String> tagNames;

    @XmlElement(name = "description")
    private String description;

    public AddressEntry() {
    }

    public AddressEntry(String name, String netmask, String description, List<String> tagNames) {
        this.name = name;
        this.netmask = netmask;
        this.description = description;
        this.tagNames = tagNames;
    }

    public String getNetmask() {
        return this.netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTagNames() {
        return this.tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AddressEntry [name=" + this.name + ", netmask=" + this.netmask + ", tagNames=" + this.tagNames + ", description="
                + this.description + "]";
    }
}
