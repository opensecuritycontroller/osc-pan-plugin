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

import static com.paloaltonetworks.utils.SSLContextFactory.getSSLContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.paloaltonetworks.panorama.api.mapping.TagEntry;
import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;

/**
 * Unit test for simple App.
 */
public class PanoramaApiClientIntegrationTest {
    public static final String PANORAMA_IP = "10.3.240.15";

    private static final String EXISTING_POLICY_TAG = "EXISTING_POLICY_TAG";
    private static final String EXISTING_POLICY_TAG_OTHER = "EXISTING_POLICY_TAG_OTHER";
    private static final String TEST_SETUP_MSG = String.format("Test setup expects the following shared tags on panorama %s: %s and %s!",
                                                    PANORAMA_IP, EXISTING_POLICY_TAG, EXISTING_POLICY_TAG_OTHER);

    private static final String XPATH_SHARED_TAG = "/config/shared/tag";

    private Client client;
    private PanoramaApiClient panClient;

    @Before
    public void setup() throws Exception {
        this.client = ClientBuilder.newBuilder().sslContext(getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();
        this.panClient = new PanoramaApiClient(PANORAMA_IP, 443, true, "admin", "admin",
                this.client);
    }

    @After
    public void cleanup() {
        this.client.close();
    }

    @Ignore
    @Test
    public void testListPolicyTags() throws Exception {
        List<TagEntry> tags = this.panClient.getTagEntries(XPATH_SHARED_TAG);
        assertNotNull(tags);
        assertTrue(TEST_SETUP_MSG, tags.size() > 1);
        assertTrue(tags.stream().anyMatch(t -> EXISTING_POLICY_TAG.equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> EXISTING_POLICY_TAG_OTHER.equals(t.getName())));
        assertTrue(this.panClient.tagExists(EXISTING_POLICY_TAG, XPATH_SHARED_TAG));
        assertTrue(this.panClient.tagExists(EXISTING_POLICY_TAG_OTHER, XPATH_SHARED_TAG));
    }
}
