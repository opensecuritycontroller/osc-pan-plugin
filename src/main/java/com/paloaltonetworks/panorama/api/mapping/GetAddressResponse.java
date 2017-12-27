package com.paloaltonetworks.panorama.api.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class GetAddressResponse implements PANResponse {
    @XmlAttribute(name = "status")
    private String status;

    @XmlAttribute(name = "code")
    private String code;

    @XmlElement(name = "result")
    private GetAddressResult result;

    @Override
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GetAddressResult getResult() {
        return this.result;
    }

    public void setResult(GetAddressResult result) {
        this.result = result;
    }
}
