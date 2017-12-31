package com.paloaltonetworks.osc.api;

import static com.paloaltonetworks.utils.SSLContextFactory.getSSLContext;
import static org.junit.Assert.assertNotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;
import com.paloaltonetworks.utils.VsToDevGroupNameUtil;

public abstract class AbstractPANApiIntegrationTest {

    protected static final String EXISTING_POLICY_TAG = "EXISTING_POLICY_TAG";
    protected static final String EXISTING_POLICY_TAG_OTHER = "EXISTING_POLICY_TAG_OTHER";
    protected static final long VS_ID = 32123L;

    private static final String PANORAMA_IP = "10.3.240.15";

    protected PanoramaApiClient panClient;

    @Mock
    protected ApplianceManagerConnectorElement mgrConnector;
    @Mock
    protected VirtualSystemElement vs;

    protected String devGroup;

    private Client client;

    @SuppressWarnings("boxing")
    protected void setup() throws Exception {
        this.client = ClientBuilder.newBuilder().sslContext(getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

        this.panClient = new PanoramaApiClient(PANORAMA_IP, 443, true, "admin", "admin",
                this.client);

        MockitoAnnotations.initMocks(this);
        Mockito.when(this.mgrConnector.getName()).thenReturn("OSCTestAppMgr");
        Mockito.when(this.vs.getName()).thenReturn("OSCTestVs");
        Mockito.when(this.vs.getId()).thenReturn(32123L);
        Mockito.when(this.vs.getMgrId()).thenReturn("" + VS_ID);

        PANDeviceApi devApi = new PANDeviceApi(this.mgrConnector, this.vs, this.panClient);
        if (devApi.getDeviceById(this.vs.getId().toString()) == null) {
            devApi.createVSSDevice();
            ManagerDeviceElement mde = devApi.getDeviceById(this.vs.getId().toString());
            assertNotNull(mde);
        }

        this.devGroup = VsToDevGroupNameUtil.devGroupName(this.vs.getId());
    }

    public void cleanup() {
        this.client.close();
    }
}
