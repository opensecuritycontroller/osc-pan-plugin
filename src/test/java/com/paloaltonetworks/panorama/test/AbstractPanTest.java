package com.paloaltonetworks.panorama.test;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ApplianceSoftwareVersionElement;
import org.osc.sdk.manager.element.DistributedApplianceElement;
import org.osc.sdk.manager.element.DomainElement;
import org.osc.sdk.manager.element.ManagerTypeElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osc.sdk.manager.element.VirtualizationConnectorElement;

import com.paloaltonetworks.panorama.api.methods.JAXBProvider;

public class AbstractPanTest {
    protected static final Logger log = Logger.getLogger(AbstractPanTest.class);

    public static final String USER = "admin";
    public static final String PASSWORD = "admin";
    public static final String KEYGEN_TYPE = "keygen";

    public static final String SESSION = "DEADBEEF";

    protected int serverPort;

    private Server server;

    /**
     * This is a test resource which mocks Panorama Security Manager
     */
    @Path("/api")
    public static class TestResource {

        @GET
        @Produces(MediaType.APPLICATION_XML)
        public Response login(@QueryParam("type") String type, @QueryParam("user") String user,
                @QueryParam("password") String password) {

            if (USER.equals(user) && PASSWORD.equals(password) && KEYGEN_TYPE.equals(type)) {

                return Response
                        .ok("<response status=\"success\"><result><key>gJlQWE56987nBxIqyfa62sZeRtYuIo2BgzEA9UOnlZBhU</key></result></response>")
                        .build();

            }

            return Response.status(Status.FORBIDDEN).build();
        }
    }

    @Before
    public void setupServer() {
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(0).build();
        ResourceConfig config = new ResourceConfig(TestResource.class, JAXBProvider.class);
        this.server = JettyHttpContainerFactory.createServer(baseUri, config);
        this.serverPort = ((ServerConnector) this.server.getConnectors()[0]).getLocalPort();

        System.out.println("Server :" + this.server.toString() + "  & port : " + this.serverPort);
    }

    @After
    public void tearDownServer() throws Exception {
        this.server.stop();
    }

    protected ApplianceManagerConnectorElement getConnector() {
        ApplianceManagerConnectorElement amce = new ApplianceManagerConnectorElement() {

            @Override
            public String getUsername() {
                return USER;
            }

            @Override
            public byte[] getPublicKey() {
                return null;
            }

            @Override
            public String getPassword() {
                return PASSWORD;
            }

            @Override
            public String getName() {
                return "TESTING";
            }

            @Override
            public ManagerTypeElement getManagerType() {
                return null;
            }

            @Override
            public String getLastKnownNotificationIpAddress() {
                return "127.0.0.1";
            }

            @Override
            public String getIpAddress() {
                return "127.0.0.1";
            }

            @Override
            public String getClientIpAddress() {
                return "127.0.0.1";
            }

            @Override
            public String getApiKey() {
                return null;
            }
        };
        return amce;
    }

    protected VirtualSystemElement getVirtualSystem() {
        // TODO Auto-generated method stub
        return new VirtualSystemElement() {

            @Override
            public VirtualizationConnectorElement getVirtualizationConnector() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getMgrId() {
                return "1002";
            }

            @Override
            public byte[] getKeyStore() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long getId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public DomainElement getDomain() {
                return new DomainElement() {

                    @Override
                    public String getMgrId() {
                        return "0";
                    }
                };
            }

            @Override
            public DistributedApplianceElement getDistributedAppliance() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ApplianceSoftwareVersionElement getApplianceSoftwareVersion() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}
