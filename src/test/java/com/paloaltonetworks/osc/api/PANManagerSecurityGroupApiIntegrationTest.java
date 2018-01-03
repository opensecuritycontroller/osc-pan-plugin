package com.paloaltonetworks.osc.api;

import static com.paloaltonetworks.utils.TagToSGIdUtil.securityGroupTag;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osc.sdk.manager.element.SecurityGroupMemberElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;

public class PANManagerSecurityGroupApiIntegrationTest extends AbstractPANApiIntegrationTest {

    private static final String SG_ID = "123454321";
    private static final String SGNAME = "123454321";

    @Mock
    private SecurityGroupMemberElement sgMember1;

    @Mock
    private SecurityGroupMemberElement sgMember2;

    @Mock
    private SecurityGroupMemberListElement with1;

    @Mock
    private SecurityGroupMemberListElement with2;

    @Mock
    private SecurityGroupMemberListElement with1and2;

    private PANManagerSecurityGroupApi sgApi;
    private String sgTag;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();

        this.sgApi = new PANManagerSecurityGroupApi(this.mgrConnector, this.vs, this.panClient);

        MockitoAnnotations.initMocks(this);

        Mockito.when(this.sgMember1.getId()).thenReturn(SG_ID);
        Mockito.when(this.sgMember1.getName()).thenReturn("sgMember_" + SG_ID);
        Mockito.when(this.sgMember1.getIpAddresses()).thenReturn(asList("1.1.1.1"));
        Mockito.when(this.sgMember1.getMacAddresses()).thenReturn(asList("1.1.1.1"));

        Mockito.when(this.sgMember2.getId()).thenReturn(SG_ID);
        Mockito.when(this.sgMember2.getName()).thenReturn("sgMember_" + SG_ID);
        Mockito.when(this.sgMember2.getIpAddresses()).thenReturn(asList("2.2.2.2"));
        Mockito.when(this.sgMember2.getMacAddresses()).thenReturn(asList("2.2.2.2"));

        Mockito.when(this.with1.getMembers()).thenReturn(asList(this.sgMember1));
        Mockito.when(this.with2.getMembers()).thenReturn(asList(this.sgMember2));
        Mockito.when(this.with1and2.getMembers()).thenReturn(asList(this.sgMember1, this.sgMember2));

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
        List<AddressEntry> addresses =  this.panClient.fetchAddressesWithTag(this.sgTag, this.devGroup);
        assertEquals(0, addresses.size());
        String sgMgrId = this.sgApi.createSecurityGroup(SGNAME, SG_ID, this.with1and2);
        addresses =  this.panClient.fetchAddressesWithTag(this.sgTag, this.devGroup);
        assertEquals(2, addresses.size());
        assertEquals(addresses.get(0).getName(), "1.1.1.1");

        this.sgApi.updateSecurityGroup(sgMgrId, SGNAME, this.with2);
        addresses =  this.panClient.fetchAddressesWithTag(this.sgTag, this.devGroup);
        assertEquals(1, addresses.size());
        assertEquals(addresses.get(0).getName(), "2.2.2.2");

        this.sgApi.updateSecurityGroup(sgMgrId, SGNAME, this.with1and2);
        addresses =  this.panClient.fetchAddressesWithTag(this.sgTag, this.devGroup);
        assertEquals(2, addresses.size());
        Assert.assertTrue(addresses.stream().anyMatch(e -> "1.1.1.1".equals(e.getName())));
        Assert.assertTrue(addresses.stream().anyMatch(e -> "2.2.2.2".equals(e.getName())));

        this.sgApi.updateSecurityGroup(sgMgrId, SGNAME, this.with2);
        addresses =  this.panClient.fetchAddressesWithTag(this.sgTag, this.devGroup);
        assertEquals(1, addresses.size());
        assertEquals(addresses.get(0).getName(), "2.2.2.2");

        this.sgApi.deleteSecurityGroup(sgMgrId);
        addresses =  this.panClient.fetchAddressesWithTag(this.sgTag, this.devGroup);
        assertEquals(0, addresses.size());
        addresses =  this.panClient.getAddressEntries(this.devGroup);
        assertEquals(0, addresses.size());
    }
}
