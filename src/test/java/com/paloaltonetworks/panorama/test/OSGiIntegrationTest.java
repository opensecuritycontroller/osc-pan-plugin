package com.paloaltonetworks.panorama.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osc.sdk.manager.api.ApplianceManagerApi;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGiIntegrationTest extends AbstractPanTest {

    @Inject
    ConfigurationAdmin configAdmin;

    @Inject
    BundleContext context;

    private ServiceTracker<ApplianceManagerApi, ApplianceManagerApi> tracker;

    @org.ops4j.pax.exam.Configuration
    public Option[] config() {

        return options(

                // Load the current module from its built classes so we get the latest from Eclipse
                bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),

                // The test server needs access to the internal model and rest packages, which
                // are not exported (and should not be). We add this fragment so that the test
                // server can use them.
                CoreOptions.streamBundle(TinyBundles.bundle().set(Constants.BUNDLE_MANIFESTVERSION, "2")
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Fragment")
                        .set(Constants.FRAGMENT_HOST, "Panorama")
                        .set(Constants.EXPORT_PACKAGE,
                                "com.paloaltonetworks.osc.model,com.paloaltonetworks.panorama.api.mapping,"
                                        + "com.paloaltonetworks.panorama.api.methods, com.paloaltonetworks.utils")
                        .build()).noStart(),

                // And some dependencies

                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),

                mavenBundle("org.osc.api", "security-mgr-api").versionAsInProject(),
                mavenBundle("javax.websocket", "javax.websocket-api").versionAsInProject(),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(),

                mavenBundle("commons-codec", "commons-codec").versionAsInProject(),
                mavenBundle("javax.ws.rs", "javax.ws.rs-api").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-client").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-common").versionAsInProject(),
                mavenBundle("javax.annotation", "javax.annotation-api").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.bundles.repackaged", "jersey-guava").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-api").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-utils").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "osgi-resource-locator").versionAsInProject(),
                mavenBundle("org.glassfish.hk2.external", "aopalliance-repackaged").versionAsInProject(),

                //               mavenBundle("org.glassfish.jersey.media", "jersey-media-jaxb").versionAsInProject(),

                // Just needed for the test so we can configure the client to point at the local test server
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.10"),

                // Needed for testing and the test server
                mavenBundle("org.glassfish.jersey.core", "jersey-server").versionAsInProject(),
                mavenBundle("javax.servlet", "javax.servlet-api").versionAsInProject(),
                mavenBundle("javax.validation", "validation-api").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-locator").versionAsInProject(),
                mavenBundle("org.javassist", "javassist").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.containers", "jersey-container-jetty-http").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-http").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-server").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-util").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-io").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-continuation").versionAsInProject(),

                // Uncomment this line to allow remote debugging
                // CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),

                junitBundles());
    }

    @Before
    public void setup() throws IOException {
        System.out.println("reference:file:" + PathUtils.getBaseDir() + "/target/classes/");

        Configuration configuration = this.configAdmin
                .getConfiguration("com.paloaltonetworks.panorama.ApplianceManager", "?");

        Dictionary<String, Object> config = new Hashtable<>();
        config.put("use.https", false);
        config.put("port", this.serverPort);
        config.put("testing", true);

        configuration.update(config);

        // Set up a tracker which only picks up "testing" services
        this.tracker = new ServiceTracker<ApplianceManagerApi, ApplianceManagerApi>(this.context,
                ApplianceManagerApi.class, null) {
            @Override
            public ApplianceManagerApi addingService(ServiceReference<ApplianceManagerApi> ref) {
                if (Boolean.TRUE.equals(ref.getProperty("testing"))) {
                    return this.context.getService(ref);
                }
                return null;
            }
        };

        this.tracker.open();
    }

    @After
    public void tearDown() throws IOException, InvalidSyntaxException {
        this.tracker.close();
        Configuration[] configs = this.configAdmin
                .listConfigurations("(service.pid=com.paloaltonetworks.panorama.ApplianceManager)");

        if (configs != null) {
            //There should be exactly one config as we searched by pid
            configs[0].delete();
        }
    }

    @Test
    public void testRegistered() throws InterruptedException {

        ApplianceManagerApi apiService = this.tracker.waitForService(5000);

        assertNotNull(apiService);

    }

    @Test
    public void testBasicConnect() throws Exception {

        ApplianceManagerApi apiService = this.tracker.waitForService(5000);

        assertNotNull(apiService);

        apiService.checkConnection(getConnector());

    }

}
