package com.paloaltonetworks.panorama.api.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "response")
@XmlSeeAlso({GetTagResponse.class})
public class PANEntryListResponse<T> {
    private String status;

    private String code;

    @XmlAttribute(name = "status")
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlAttribute(name = "code")
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public PANResponseEntryListResult<T> getResult() {
        return null;
    };
}
