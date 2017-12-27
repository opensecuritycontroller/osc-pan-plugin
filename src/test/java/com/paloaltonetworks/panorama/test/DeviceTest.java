package com.paloaltonetworks.panorama.test;

import java.security.SecureRandom;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
/*
 * This is an environment specific Test.
 * To run this test, change the IP, username & password variables pointing to the device
 * and then uncomment the @Ignore annotation.
 */
import org.slf4j.LoggerFactory;

import com.paloaltonetworks.panorama.api.methods.PanoramaApiClient;

public class DeviceTest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String IP = "10.71.85.99";
    private static final String PAN_OS_ID = "007299000003740";

    private static final Logger LOG = LoggerFactory.getLogger(DeviceTest.class);

    private PanoramaApiClient panClient;
    private Client client;

    @Ignore
    @Before
    public void start() throws Exception {

        this.client = ClientBuilder.newBuilder().sslContext(getSSLContext()).hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();

        boolean isHttps = true;
        this.panClient = new PanoramaApiClient(IP, 443, isHttps, USERNAME, PASSWORD, PAN_OS_ID, this.client);

    }


    public static SSLContext getSSLContext() {
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

            LOG.error("Encountering security exception", ex);
        }

        return ctx;
    }

    @Ignore
    @Test
    public void test() throws Exception{

        boolean expected = this.panClient.checkConnection();
        Assert.assertTrue(expected);

        List<String> devices = this.panClient.showDevices();
        System.out.println( this.getClass().getSimpleName()+ " Devices list Size is : "+devices.size());
        for (String device : devices) {
            System.out.println( this.getClass().getSimpleName()+ " Device : "+device);
        }

        showDeviceGroups();

//        String dgName = "dg dec28";
//        String status = this.panClient.AddDeviceGroup(dgName, "Device Group to be deleted");
//        if(status.equals("success")) {
//            System.out.println( this.getClass().getSimpleName()+ " Device group : "+ dgName+" added successfully");
//        }
//
//        showDeviceGroups();
//
//        status="failure";
//        status = this.panClient.DeleteDeviceGroup(dgName);
//        if(status.equals("success")) {
//            System.out.println( this.getClass().getSimpleName()+ " Device group : "+ dgName+" deleted successfully");
//        }
//
//        showDeviceGroups();
    }

    @Ignore
    @Test
    public void test3() throws Exception{

        String dgTag = "Tag-Dec25";
        String status = this.panClient.addDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println(this.getClass().getSimpleName()+ " Successfully added TAG: " + dgTag);
        }

        boolean state = this.panClient.policyTagExists(dgTag);
        if (state == true) {
            System.out.println(this.getClass().getSimpleName()+" TAG: " + dgTag + "exists");
        }
    }

    @Ignore
    @Test
    public void test4() throws Exception{

        String dgTag = "Tag-Dec25";
        String status = this.panClient.deleteDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println(this.getClass().getSimpleName()+ " Successfully deleted TAG: " + dgTag);
        }

        boolean state = this.panClient.policyTagExists(dgTag);
        if (state == true) {
            System.out.println(this.getClass().getSimpleName()+" TAG: " + dgTag + "exists");
        } else {
            System.out.println(this.getClass().getSimpleName()+" TAG: " + dgTag + "doesn't exists");

        }
    }

    @Ignore
    @Test
    public void test2() throws Exception {

        showDeviceGroups();

        String status = this.panClient.deleteDeviceGroup("testing");
        if(status.equals("success")) {
            System.out.println( this.getClass().getSimpleName()+ " Device group : "+ "testing"+" deleted successfully");
        }

        showDeviceGroups();

    }
    private void showDeviceGroups() throws Exception{
        List<String> dgList = this.panClient.showDeviceGroups();
        System.out.println( this.getClass().getSimpleName()+ " Device Groups");
        System.out.println("------------------------------------------------------ ");
        for (String dg : dgList) {
            System.out.println( dg);
        }
    }

    @Ignore
    @After
    public void stop(){

    }

}
