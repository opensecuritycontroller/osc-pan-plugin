package com.paloaltonetworks.osc.api;

import static com.paloaltonetworks.utils.TagToSGIdUtil.securityGroupTag;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupMemberElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;

public class PANManagerSecurityGroupApiInterfaceIntegrationTest
    extends AbstractPANApiIntegrationTest {
    private static final String SG_ID = "123454321";
    private static final String SGNAME = "123454321";
    private static final String SG_INTERFACE_ID = "6789876";

    @Mock
    private SecurityGroupMemberElement sgMember1;

    @Mock
    private SecurityGroupMemberListElement with1;

    @Mock
    private SecurityGroupInterfaceElement sgInterfaceElement;

    @Mock
    private ManagerPolicyElement existingPolicyTag;

    private PANManagerSecurityGroupApi sgApi;
    private PANManagerSecurityGroupInterfaceApi sgInterfaceApi;
    private String sgTag;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();

        this.sgApi = new PANManagerSecurityGroupApi(this.vs, this.panClient);
        this.sgInterfaceApi = new PANManagerSecurityGroupInterfaceApi(this.vs, this.panClient);

        MockitoAnnotations.initMocks(this);
        // OSC_SecurityGroup_

        Mockito.when(this.sgMember1.getIpAddresses()).thenReturn(asList("1.1.1.1"));
        Mockito.when(this.sgMember1.getMacAddresses()).thenReturn(asList("1.1.1.1"));

        Mockito.when(this.with1.getMembers()).thenReturn(asList(this.sgMember1));

        Mockito.when(this.existingPolicyTag.getName()).thenReturn(EXISTING_POLICY_TAG);

        Set<ManagerPolicyElement> policyElements = new HashSet<>();
        policyElements.add(this.existingPolicyTag);

        Mockito.when(this.sgInterfaceElement.getManagerSecurityGroupInterfaceId()).thenReturn(SG_INTERFACE_ID);
        Mockito.when(this.sgInterfaceElement.getManagerSecurityGroupId()).thenReturn("OSCSecurityGroup_" + SG_ID);
        Mockito.when(this.sgInterfaceElement.getManagerPolicyElements()).thenReturn(policyElements);

        this.sgTag = securityGroupTag(SG_ID);
    }

    @Override
    @After
    public void cleanup() {
        super.cleanup();
    }

    @Ignore
    @Test
    public void testCRUDOperations() throws Exception {
        List<AddressEntry> addresses =  this.panClient.fetchAddressesWithTag(EXISTING_POLICY_TAG, this.devGroup);
        assertEquals(0, addresses.size());

        this.sgTag = this.sgApi.createSecurityGroup(SGNAME, SG_ID, this.with1);
        addresses =  this.panClient.fetchAddressesWithTag(EXISTING_POLICY_TAG, this.devGroup);
        assertEquals(0, addresses.size());

        String sgInterfaceId = this.sgInterfaceApi.createSecurityGroupInterface(this.sgInterfaceElement);
        assertEquals(this.sgTag + "_" + this.devGroup, sgInterfaceId);

        addresses =  this.panClient.fetchAddressesWithTag(EXISTING_POLICY_TAG, this.devGroup);
        assertEquals(1, addresses.size());
        assertEquals(addresses.get(0).getName(), "1.1.1.1");

        this.sgInterfaceApi.deleteSecurityGroupInterface(sgInterfaceId);
        addresses =  this.panClient.fetchAddressesWithTag(EXISTING_POLICY_TAG, this.devGroup);
        assertEquals(0, addresses.size());

        this.sgApi.deleteSecurityGroup(this.sgTag);
    }
}
