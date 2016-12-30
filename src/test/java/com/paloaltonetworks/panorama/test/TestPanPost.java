package com.paloaltonetworks.panorama.test;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.junit.Test;

import com.paloaltonetworks.panorama.api.mapping.ShowResponse;
import com.paloaltonetworks.panorama.api.methods.JAXBProvider;

public class TestPanPost extends AbstractPanTest {
    private static final Logger log = Logger.getLogger(TestPanPost.class);
    private static final String URL = "http://%s:%s/api";
    private static final String LOCALHOST = "127.0.0.1";
    private Client client;


    @Test
    public void test() {

        this.client = ClientBuilder
                .newBuilder()
                .register(JAXBProvider.class)
                .sslContext(getSSLContext())
                .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                .build();

        UriBuilder uriBuilder = (UriBuilder.fromPath("/api").scheme("http").host(LOCALHOST).port(this.serverPort));

        Map<String, String> queryStrings = new HashMap<>();
        queryStrings.put("user", USER);
        queryStrings.put("password", PASSWORD);
        queryStrings.put("type", "keygen");

        for (String key : queryStrings.keySet()) {
            String value = queryStrings.get(key);
            uriBuilder.queryParam(key, value);
        }


        try {
            ShowResponse showResponse = this.client.target(uriBuilder)
            .request().accept(MediaType.APPLICATION_XML).buildGet().submit(ShowResponse.class).get(60, TimeUnit.SECONDS);

            String status = showResponse.getStatus();
            String key = showResponse.getShowResult().getKey();
            System.out.println("Status:" + status + " & Key = " + key);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.client.close();
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
        }  };
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAllCerts, new SecureRandom());

        } catch (java.security.GeneralSecurityException ex) {

            log.error("Encountering security exception", ex);
        }

        return ctx;
    }


}
