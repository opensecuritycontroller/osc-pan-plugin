package com.paloaltonetworks.panorama.api.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result")
public class GetAddressResult {

    @XmlElementWrapper(name = "address")
    @XmlElement(name = "entry", type = AddressEntry.class)
    private List<AddressEntry> entries;

    public List<AddressEntry> getEntries() {
        return this.entries;
    }

    public void setEntries(List<AddressEntry> entries) {
        this.entries = entries;
    }
}
