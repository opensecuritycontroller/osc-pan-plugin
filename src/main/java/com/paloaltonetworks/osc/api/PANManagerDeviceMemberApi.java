package com.paloaltonetworks.osc.api;

import java.util.Collections;
import java.util.List;

import org.osc.sdk.manager.api.ManagerDeviceMemberApi;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberStatusElement;

public class PANManagerDeviceMemberApi implements ManagerDeviceMemberApi {

    @Override
    public List<ManagerDeviceMemberStatusElement> getFullStatus(List<DistributedApplianceInstanceElement> list) {
        return Collections.emptyList();
    }

    @Override
    public void reAuthenticateAppliance() {

    }

    @Override
    public void syncAgent() {
    }
}
