package com.paloaltonetworks.osc.api;

import java.util.Collections;
import java.util.List;

import org.osc.sdk.manager.api.ManagerDeviceMemberApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberStatusElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

public class PANManagerDeviceMemberApi implements ManagerDeviceMemberApi {

    VirtualSystemElement vs;
    ApplianceManagerConnectorElement mc;

    public static ManagerDeviceMemberApi create(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
        return new PANManagerDeviceMemberApi(mc, vs);
    }

    private PANManagerDeviceMemberApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) {
        this.vs = vs;
        this.mc = mc;
    }

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
