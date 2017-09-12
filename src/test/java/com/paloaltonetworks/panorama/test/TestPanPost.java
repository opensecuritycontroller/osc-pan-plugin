package com.paloaltonetworks.panorama.test;

import static org.junit.Assert.assertEquals;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.osc.api.PANDeviceApi;
import com.paloaltonetworks.osc.api.PANManagerSecurityGroupInterfaceApi;
import com.paloaltonetworks.panorama.api.mapping.ShowResponse;
import com.paloaltonetworks.panorama.api.methods.JAXBProvider;
import com.paloaltonetworks.panorama.api.methods.ShowOperations;

public class TestPanPost extends AbstractPanTest {
    private static final Logger log = Logger.getLogger(TestPanPost.class);
    private static final String URL = "http://%s:%s/api";
    private static final String LOCALHOST = "127.0.0.1";
    private Client client;

    @Before
    public void setUp() {
        this.client = ClientBuilder.newBuilder().register(JAXBProvider.class).sslContext(getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

    }

    @After
    public void close() {

        this.client.close();
    }

    @Test
    public void testLogin() throws Exception {

        UriBuilder uriBuilder = (UriBuilder.fromPath("/api").scheme("http").host(LOCALHOST).port(this.serverPort));

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put(USER_PARAM, USERNAME);
        queryStrings.put(PASSWORD_PARAM, PASSWORD);
        queryStrings.put(TYPE_PARAM, KEYGEN);

        for (String key : queryStrings.keySet()) {
            String value = queryStrings.get(key);
            uriBuilder.queryParam(key, value);
        }

        ShowResponse showResponse = this.client.target(uriBuilder).request().accept(MediaType.APPLICATION_XML)
                .buildGet().submit(ShowResponse.class).get(60, TimeUnit.SECONDS);

        String status = showResponse.getStatus();
        String key = showResponse.getShowResult().getKey();

        Assert.assertEquals("success", status);
        Assert.assertEquals(APIKEY, key);
    }

    @Test
    public void testCreateSecurityGroupInterface() throws Exception {
        ApplianceManagerConnectorElement amce = getConnector();
        VirtualSystemElement vse = getVirtualSystem();

        ShowOperations showOperations = new ShowOperations(LOCALHOST, this.serverPort, false, USERNAME, PASSWORD,
                this.client);
        PANManagerSecurityGroupInterfaceApi sgiApi = new PANManagerSecurityGroupInterfaceApi(amce, vse, showOperations);
        SecurityGroupInterfaceElement sgiElement = new SecurityGroupInterfaceElement() {

			@Override
			public String getTag() {
				return "Tag1";
			}

			@Override
			public String getSecurityGroupInterfaceId() {
				return null;
			}

			@Override
			public String getName() {
				return "Tag Name";
			}

			@Override
			public String getManagerSecurityGroupId() {
				return null;
			}

			@Override
			public Set<String> getManagerPolicyIds() {
				return null;
			}
		};
        String result = sgiApi.createSecurityGroupInterface(sgiElement);

        assertEquals("Tag Name", result);

    }


    private static SSLContext getSSLContext() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                    throws java.security.cert.CertificateException {

            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                    throws java.security.cert.CertificateException {

            }
        } };
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAllCerts, new SecureRandom());

        } catch (java.security.GeneralSecurityException ex) {

            log.error("Encountering security exception", ex);
        }

        return ctx;
    }

    @Test
    public void testDeviceListing() throws Exception {
        ApplianceManagerConnectorElement amce = getConnector();
        VirtualSystemElement vse = getVirtualSystem();

        ShowOperations showOperations = new ShowOperations(LOCALHOST, this.serverPort, false, USERNAME, PASSWORD,
                this.client);
        PANDeviceApi deviceApi = new PANDeviceApi(amce, vse, showOperations);
        String id = deviceApi.findDeviceByName("Pan-NGFW-5");

        assertEquals("Pan-NGFW-5", id);

    }

    @Test
    public void testDeviceDetail() throws Exception {
        ApplianceManagerConnectorElement amce = getConnector();
        VirtualSystemElement vse = getVirtualSystem();

        ShowOperations showOperations = new ShowOperations(LOCALHOST, this.serverPort, false, USERNAME, PASSWORD,
                this.client);
        PANDeviceApi deviceApi = new PANDeviceApi(amce, vse, showOperations);
        ManagerDeviceElement id = deviceApi.getDeviceById("Pan-NGFW-10");

        assertEquals("Pan-NGFW-10", id.getId());

    }

    @Test
    public void testMemberDevices() throws Exception {
        ApplianceManagerConnectorElement amce = getConnector();
        VirtualSystemElement vse = getVirtualSystem();

        ShowOperations showOperations = new ShowOperations(LOCALHOST, this.serverPort, false, USERNAME, PASSWORD,
                this.client);
        PANDeviceApi deviceApi = new PANDeviceApi(amce, vse, showOperations);
        List<? extends ManagerDeviceMemberElement> devices = deviceApi.listDeviceMembers();

        assertEquals(0, devices.size());

    }

}
