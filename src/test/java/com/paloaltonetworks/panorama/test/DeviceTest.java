package com.paloaltonetworks.panorama.test;

import java.security.SecureRandom;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.paloaltonetworks.panorama.api.methods.ShowOperations;
/*
 * This is an environment specific Test.
 *
 *
 */

public class DeviceTest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String IP = "10.71.85.99";

    private static final Logger LOG = Logger.getLogger(DeviceTest.class);

    private ShowOperations showOperations;
    private Client client;

    @Before
    public void start() {

        this.client = ClientBuilder.newBuilder().sslContext(getSSLContext()).hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();

        boolean isHttps = true;
        this.showOperations = new ShowOperations(IP, 443, isHttps, USERNAME, PASSWORD, this.client);

    }


    public static SSLContext getSSLContext() {
        // TODO: Future. We trust all managers right now. Later we need to import certificates and verify every connection with
        // given Trust store
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
    public void test() {

        boolean expected = this.showOperations.checkConnection();
        Assert.assertTrue(expected);

        ArrayList<String> devices = this.showOperations.ShowDevices();
        System.out.println( this.getClass().getSimpleName()+ " Devices list Size is : "+devices.size());
        for (String device : devices) {
            System.out.println( this.getClass().getSimpleName()+ " Device : "+device);
        }

        showDeviceGroups();

//        String dgName = "dg dec28";
//        String status = this.showOperations.AddDeviceGroup(dgName, "Device Group to be deleted");
//        if(status.equals("success")) {
//            System.out.println( this.getClass().getSimpleName()+ " Device group : "+ dgName+" added successfully");
//        }
//
//        showDeviceGroups();
//
//        status="failure";
//        status = this.showOperations.DeleteDeviceGroup(dgName);
//        if(status.equals("success")) {
//            System.out.println( this.getClass().getSimpleName()+ " Device group : "+ dgName+" deleted successfully");
//        }
//
//        showDeviceGroups();
    }

    @Ignore
    @Test
    public void test3() {

        String dgTag = "Tag-Dec25";
        String status = this.showOperations.AddDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println(this.getClass().getSimpleName()+ " Successfully added TAG: " + dgTag);
        }

        boolean state = this.showOperations.TagExists(dgTag);
        if (state == true) {
            System.out.println(this.getClass().getSimpleName()+" TAG: " + dgTag + "exists");
        }

        status = this.showOperations.ShowDAGTag();
        if (status.equals("success")) {
            System.out.println(this.getClass().getSimpleName()+"Successfully displayed All tags ");
        }



    }

    @Ignore
    @Test
    public void test4() {

        String dgTag = "Tag-Dec25";
        String status = this.showOperations.DeleteDAGTag(dgTag);
        if (status.equals("success")) {
            System.out.println(this.getClass().getSimpleName()+ " Successfully deleted TAG: " + dgTag);
        }

        boolean state = this.showOperations.TagExists(dgTag);
        if (state == true) {
            System.out.println(this.getClass().getSimpleName()+" TAG: " + dgTag + "exists");
        } else {
            System.out.println(this.getClass().getSimpleName()+" TAG: " + dgTag + "doesn't exists");

        }

        status = this.showOperations.ShowDAGTag();
        if (status.equals("success")) {
            System.out.println(this.getClass().getSimpleName()+"Successfully displayed All tags ");
        }



    }

    @Ignore
    @Test
    public void test2() {

        showDeviceGroups();

        String status = this.showOperations.DeleteDeviceGroup("testing");
        if(status.equals("success")) {
            System.out.println( this.getClass().getSimpleName()+ " Device group : "+ "testing"+" deleted successfully");
        }

        showDeviceGroups();

    }
    private void showDeviceGroups() {
        ArrayList<String> dgList = this.showOperations.ShowDeviceGroups();
        System.out.println( this.getClass().getSimpleName()+ " Device Groups");
        System.out.println("------------------------------------------------------ ");
        for (String dg : dgList) {
            System.out.println( dg);
        }
    }

    @After
    public void stop(){

    }

}
