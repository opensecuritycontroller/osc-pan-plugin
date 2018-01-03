package com.paloaltonetworks.osc.model;

import org.osc.sdk.manager.element.ManagerSecurityGroupElement;

public class PANSecurityGroupElement implements ManagerSecurityGroupElement{

    private String sgId;
    private String name;

    public PANSecurityGroupElement(String sgId, String name) {
        super();
        this.sgId = sgId;
        this.name = name;
    }

    @Override
    public String getSGId() {
        return this.sgId;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
}
