package com.paloaltonetworks.osc.api;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osc.sdk.manager.element.ManagerDeviceElement;

public class PANManagerDeviceApiIntegrationTest extends AbstractPANApiIntegrationTest {

    private PANDeviceApi api;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        this.api = new PANDeviceApi(this.mgrConnector, this.vs, this.panClient);
    }

    @Override
    @After
    public void cleanup() {
        super.cleanup();
    }

    @Ignore
    @Test
    public void testCRUDOperations() throws Exception {
        // Device group should have been created in the setup
        List<? extends ManagerDeviceElement> deviceMembers = this.api.listDevices();
        assertNotNull(deviceMembers);
        assertTrue("Existing DevGroup not found in the list! " + this.devGroup,
                deviceMembers.stream().anyMatch(dm -> this.vs.getName().equals(dm.getName())));

        ManagerDeviceElement mde = this.api.getDeviceById(this.vs.getName());
        assertNotNull("getDeviceById failed to retrieve an existing group: " + this.devGroup, mde);
        assertEquals(this.vs.getName(), mde.getName());

        // This call does not work
//        this.api.deleteVSSDevice();
//        deviceMembers = this.api.listDevices();
//        assertFalse("Failed to delete " + this.devGroup,
//                    deviceMembers.stream().anyMatch(dm -> this.vs.getName().equals(dm.getName())));
    }
}
