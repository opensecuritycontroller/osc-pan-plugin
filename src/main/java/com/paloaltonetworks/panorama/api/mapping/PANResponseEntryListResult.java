package com.paloaltonetworks.panorama.api.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "result")
@XmlSeeAlso({GetTagResult.class})
public class PANResponseEntryListResult<T> {
    private List<T> entries;

    public List<T> getEntries() {
        return this.entries;
    }

    public void setEntries(List<T> entries) {
        this.entries = entries;
    }
}
