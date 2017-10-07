package com.paloaltonetworks.panorama.test;

import java.net.URI;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

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
import org.slf4j.Logger;

import com.paloaltonetworks.panorama.api.methods.JAXBProvider;
import org.slf4j.LoggerFactory;

public class AbstractPanTest {
    protected static final Logger log = LoggerFactory.getLogger(AbstractPanTest.class);

    // Authentication
    protected static final String USERNAME = "admin";
    protected static final String PASSWORD = "admin";
    protected static final String APIKEY = "gJlQWE56987nBxIqyfa62sZeRtYuIo2BgzEA9UOnlZBhU";

    // Query Params
    protected static final String USER_PARAM = "user";
    protected static final String PASSWORD_PARAM = "password";
    protected static final String TYPE_PARAM = "type";

    // Types
    protected static final String KEYGEN = "keygen";
    protected static final String OP = "op";
    protected static final String CONFIG = "config";
    protected static final String USER_ID = "user-id";
    protected static final String COMMIT = "commit";

    // Commands
    protected static final String VM_AUTH_KEY_COMMAND = "<request><bootstrap><vm-auth-key><generate><lifetime>8760</lifetime></generate></vm-auth-key></bootstrap></request>";
    protected static final String ALL_DEVICEGROUPS_COMMAND = "<show><devicegroups></devicegroups></show>";
    protected static final String ALL_DEVICES_COMMAND = "<show><devices><all></all></devices></show>";
    protected static final String COMMIT_COMMAND = "<commit></commit>";
    protected static final String ADD_DAG_COMMAND = "<uid-message><version>1.0</version><type>update</type><payload><register>}{}{}{}</register></payload></uid-message>";
    protected static final String DELETE_DAG_COMMAND = "<uid-message><version>1.0</version><type>update</type><payload><unregister>"
            + "<entry ip=\"\"><tag><member></member></tag></entry></unregister></payload></uid-message>";

    // Actions
    protected static final String SET_ACTION = "set";
    protected static final String GET_ACTION = "get";
    protected static final String DELETE_ACTION = "delete";

    // xpaths
    protected static final String DG_XPATH = "/config/devices/entry[@name='localhost.localdomain']/device-group";
    protected static final String TAG_XPATH = "/config/shared/tag";

    // element
    protected static final String DG_ELEMENT_DESCRIPTION = "<entry name='DG1'><description>DG1 description</description><devices/></entry>";
    protected static final String DG_ELEMENT = "<entry name='DG1'></entry>";
    protected static final String TAG_ELEMENT = "<entry name='Tag1'><color>color3</color><comments>OSC Tag</comments></entry>";

    // Target
    protected static final String PAN_OS_ID = "007299000003740";

    protected int serverPort;

    private Server server;

    /**
     * This is a test resource which mocks Panorama Security Manager
     */
    @Path("/api")
    public static class TestResource {

//        @GET
//        @Produces(MediaType.APPLICATION_XML)
//        public Response login(@QueryParam("type") String type, @QueryParam("user") String user,
//                @QueryParam("password") String password) {
//
//            if (USERNAME.equals(user) && PASSWORD.equals(password) && KEYGEN.equals(type)) {
//                return Response
//                        .ok("<response status=\"success\"><result><key>gJlQWE56987nBxIqyfa62sZeRtYuIo2BgzEA9UOnlZBhU</key></result></response>")
//                        .build();
//            }
//            return Response.status(Status.FORBIDDEN).build();
//        }

        @GET
        @Produces(MediaType.APPLICATION_XML)
        public Response test(@Context UriInfo info) {

            String username = info.getQueryParameters().getFirst("user");
            String password = info.getQueryParameters().getFirst("password");
            String type = info.getQueryParameters().getFirst("type");
            String apiKey = info.getQueryParameters().getFirst("key");
            String command = info.getQueryParameters().getFirst("cmd");
            String action = info.getQueryParameters().getFirst("action");
            String xpath = info.getQueryParameters().getFirst("xpath");
            String element = info.getQueryParameters().getFirst("element");
            String target = info.getQueryParameters().getFirst("target");

            switch (type) {
            case KEYGEN:

                if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                    return Response
                            .ok("<response status=\"success\"><result><key>gJlQWE56987nBxIqyfa62sZeRtYuIo2BgzEA9UOnlZBhU</key></result></response>")
                            .build();
                }
                break;

            case OP:

                if (APIKEY.equals(apiKey) && VM_AUTH_KEY_COMMAND.equals(command)) {

                    return Response
                            .ok("<response status=\"success\"><result>VM auth key 704535550245534 generated. Expires at: 2016/12/19 13:40:53</result></response>")
                            .build();
                } else if (APIKEY.equals(apiKey) && ALL_DEVICES_COMMAND.equals(command)) {

                    return Response.ok("").build();
                } else if (APIKEY.equals(apiKey) && ALL_DEVICEGROUPS_COMMAND.equals(command)) {

                        return Response.ok("<response status=\"success\"><result><devicegroups>" +
                 "<entry name=\"Pan-NGFW-5\"><shared-policy-md5sum></shared-policy-md5sum></entry>" +
                 "<entry name=\"Pan-NGFW-10\"><shared-policy-md5sum></shared-policy-md5sum></entry>" +
                 "<entry name=\"Pan-NGFW-15\"><shared-policy-md5sum></shared-policy-md5sum></entry>" +
                 "</devicegroups></result></response>").build();
                }

                break;

            case CONFIG:
                if (SET_ACTION.equals(action) && APIKEY.equals(apiKey) && DG_XPATH.equals(xpath)
                        && DG_ELEMENT_DESCRIPTION.equals(element)) {

                } else if (SET_ACTION.equals(action) && APIKEY.equals(apiKey) && TAG_XPATH.equals(xpath)
                        && TAG_ELEMENT.equals(element)) {
                    return Response
                            .ok("<response status=\"success\"><msg>Tag added</msg></response>")
                            .build();

                } else if (GET_ACTION.equals(action) && APIKEY.equals(apiKey) && TAG_XPATH.equals(xpath)) {

                } else if (DELETE_ACTION.equals(action) && APIKEY.equals(apiKey) && TAG_XPATH.equals(xpath)
                        && DG_ELEMENT.equals(element)) {

                } else if (DELETE_ACTION.equals(action) && APIKEY.equals(apiKey) && DG_XPATH.equals(xpath)
                        && DG_ELEMENT.equals(element)) {

                }
                break;
            case USER_ID:
                if (SET_ACTION.equals(action) && APIKEY.equals(apiKey) && PAN_OS_ID.equals(target)
                        && ADD_DAG_COMMAND.equals(command)) {

                } else if (SET_ACTION.equals(action) && APIKEY.equals(apiKey) && PAN_OS_ID.equals(target)
                        && DELETE_DAG_COMMAND.equals(command)) {

                }
                break;

            case COMMIT:
                if (APIKEY.equals(apiKey) && COMMIT_COMMAND.equals(command)) {

                    return Response
                            .ok("<response status=\"success\" code=\"19\"><result><msg><line>Commit job enqueued with jobid 284</line></msg><job>284</job></result></response>")
                            .build();
                }

                break;
            default:
                break;
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
                return USERNAME;
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

            @Override
            public SSLContext getSslContext() {
                return null;
            }

            @Override
            public TrustManager[] getTruststoreManager() throws Exception {
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
