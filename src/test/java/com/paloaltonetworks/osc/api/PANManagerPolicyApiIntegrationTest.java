package com.paloaltonetworks.osc.api;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.paloaltonetworks.osc.model.PolicyListElement;

public class PANManagerPolicyApiIntegrationTest extends AbstractPANApiIntegrationTest {

    private static final String EXISTING_POLICY_TAG = "EXISTING_POLICY_TAG";

    private PANManagerPolicyApi api;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();

        this.api = new PANManagerPolicyApi(this.panClient);
    }

    @Override
    @After
    public void cleanup() {
        super.cleanup();
    }

    @Ignore
    @Test
    public void testListPolicies() throws Exception {
        List<PolicyListElement> elements = this.api.getPolicyList("domain");

        assertNotNull(elements);
        assertTrue(elements.stream().anyMatch(e -> EXISTING_POLICY_TAG.equals(e.getName())));

        PolicyListElement element = this.api.getPolicy(EXISTING_POLICY_TAG, "domain");
        assertNotNull(element);
        assertEquals(EXISTING_POLICY_TAG, element.getName());
    }

    @Ignore
    @Test
    public void testGetPolicy() throws Exception {
        PolicyListElement element = this.api.getPolicy(EXISTING_POLICY_TAG, "domain");
        assertNotNull(element);
        assertEquals(EXISTING_POLICY_TAG, element.getName());
    }
}
