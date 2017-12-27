/* Copyright 2016 Palo Alto Networks Inc.
 * All Rights Reserved.
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package com.paloaltonetworks.ism;

import static org.junit.Assert.*;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.paloaltonetworks.panorama.api.mapping.AddressEntry;
import com.paloaltonetworks.panorama.api.mapping.TagEntry;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;
import com.paloaltonetworks.panorama.test.DeviceTest;

/**
 * Unit test for simple App.
 */
public class PanoramaApiClientIntegrationTest{
    private static final String PANORAMA_IP = "10.3.240.15";
    private static final String PAN_OS_ID = "007299000003740";
    private static final String EXISTING_POLICY_TAG = "EXISTING_POLICY_TAG";
    private static final String EXISTING_POLICY_TAG_OTHER = "EXISTING_POLICY_TAG_OTHER";
    private static final String TEST_SETUP_MSG = String.format("Test setup expects the following shared tags on panorama %s: %s and %s!",
                                                    PANORAMA_IP, EXISTING_POLICY_TAG, EXISTING_POLICY_TAG_OTHER);

    // TODO : move out to property
    private static final String OSC_DEVGROUP_NAME = "OpenSecurityController_Reserved";

    private Client client;
    private PanoramaApiClient panClient;

    @Before
    public void setup() throws Exception {
        this.client = ClientBuilder.newBuilder().sslContext(DeviceTest.getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();
        this.panClient = new PanoramaApiClient(PANORAMA_IP, 443, true, "admin", "admin",
                PAN_OS_ID, this.client, OSC_DEVGROUP_NAME);
    }

    @After
    public void cleanup() {
        this.client.close();
    }

    @Ignore
    @Test
    public void testListPolicyTags() throws Exception {
        List<TagEntry> tags = this.panClient.fetchPolicyTags();
        assertNotNull(tags);
        assertTrue(TEST_SETUP_MSG, tags.size() > 1);
        assertTrue(tags.stream().anyMatch(t -> EXISTING_POLICY_TAG.equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> EXISTING_POLICY_TAG_OTHER.equals(t.getName())));
        assertTrue(this.panClient.policyTagExists(EXISTING_POLICY_TAG));
        assertTrue(this.panClient.policyTagExists(EXISTING_POLICY_TAG_OTHER));
    }

    @Ignore
    @Test
    public void testAddSGTag() throws Exception {
        String tagName = "MyFancyTag";

        List<TagEntry> tags = this.panClient.fetchSGTags();
        assertEquals(0, tags.size());
        assertFalse(this.panClient.sgTagExists(tagName));

        this.panClient.addSGTag(tagName);
        assertTrue(this.panClient.sgTagExists(tagName));
        tags = this.panClient.fetchSGTags();
        assertEquals(1, tags.size());
        assertEquals(tags.get(0).getName(), tagName);

        this.panClient.deleteSGTag(tagName);
        tags = this.panClient.fetchSGTags();
        assertEquals(0, tags.size());
        assertFalse(this.panClient.sgTagExists(tagName));
    }

    @Ignore
    @Test
    public void testAddAddress() throws Exception {
        String ip = "10.2.3.4";
        String tagName = "MyFancyTag";

        List<AddressEntry> addresses = this.panClient.fetchAddresses();
        assertNotNull(addresses);
        assertEquals("The system should be preset with no addresses!", 0, addresses.size());

        this.panClient.addAddress(ip);
        addresses = this.panClient.fetchAddresses();
        assertNotNull(addresses);
        assertTrue(addresses.stream().anyMatch(a -> ip.equals(a.getName())));
        assertEquals(1, addresses.size());

        this.panClient.deleteAddress(ip);
        addresses = this.panClient.fetchAddresses();
        assertNotNull(addresses);
        assertEquals(0, addresses.size());
    }

    @Ignore
    @Test
    public void testAddSGToAddress() throws Exception {
        String ip = "10.2.3.4";
        String tagName = "MyFancyTag";

        this.panClient.addSGTag(tagName);
        List<AddressEntry> addresses = this.panClient.fetchAddressesBySGTag(tagName);
        assertNotNull(addresses);
        assertFalse(addresses.stream().anyMatch(a -> ip.equals(a.getName())));

        this.panClient.addAddress(ip);
        this.panClient.addSGTagToAddress(ip, tagName);
        addresses = this.panClient.fetchAddressesBySGTag(tagName);
        assertTrue(addresses.stream().anyMatch(a -> ip.equals(a.getName())));
        assertEquals(1, addresses.size());

        this.panClient.removeSGTagFromAddress(ip, tagName);
        addresses = this.panClient.fetchAddressesBySGTag(tagName);
        assertFalse(addresses.stream().anyMatch(a -> ip.equals(a.getName())));
        addresses = this.panClient.fetchAddresses();
        assertEquals(1, addresses.size());

        this.panClient.deleteAddress(ip);
        this.panClient.deleteSGTag(tagName);
    }

    @Ignore
    @Test
    public void tesBindPolicyToAddress() throws Exception {
        String ip = "10.2.3.4";

        List<AddressEntry> addresses = this.panClient.fetchAddressesByPolicy(EXISTING_POLICY_TAG);
        assertNotNull(addresses);
        assertEquals(0, addresses.size());

        this.panClient.addAddress(ip);

        this.panClient.bindPolicyTagToAddress(ip, EXISTING_POLICY_TAG);
        addresses = this.panClient.fetchAddressesByPolicy(EXISTING_POLICY_TAG);
        assertTrue(addresses.stream().anyMatch(a -> ip.equals(a.getName())));
        assertEquals(1, addresses.size());

        this.panClient.unbindPolicyTagFromAddress(ip, EXISTING_POLICY_TAG);
        addresses = this.panClient.fetchAddressesByPolicy(EXISTING_POLICY_TAG);
        assertFalse(addresses.contains(ip));
        assertEquals(0, addresses.size());

        this.panClient.deleteAddress(ip);
    }
}
