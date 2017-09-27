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
package com.paloaltonetworks.osc.api;

import static org.osc.sdk.manager.Constants.*;

import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.log4j.Logger;
import org.osc.sdk.manager.api.ApplianceManagerApi;
import org.osc.sdk.manager.api.IscJobNotificationApi;
import org.osc.sdk.manager.api.ManagerCallbackNotificationApi;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.api.ManagerDeviceMemberApi;
import org.osc.sdk.manager.api.ManagerDomainApi;
import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.api.ManagerWebSocketNotificationApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.paloaltonetworks.panorama.api.methods.JAXBProvider;
import com.paloaltonetworks.panorama.api.methods.ShowOperations;

@Component(configurationPid = "com.paloaltonetworks.panorama.ApplianceManager",
property={
        PLUGIN_NAME + "=PANMgrPlugin",
        VENDOR_NAME + "=Palo Alto Networks",
        SERVICE_NAME + "=PANMgrPlugin",
        EXTERNAL_SERVICE_NAME + "=Pan-nsx",
        AUTHENTICATION_TYPE + "=BASIC_AUTH",
        NOTIFICATION_TYPE + "=NONE",
        SYNC_SECURITY_GROUP + ":Boolean=true",
        PROVIDE_DEVICE_STATUS + ":Boolean=false",
        SYNC_POLICY_MAPPING + ":Boolean=false",
        SUPPORT_MULTIPLE_POLICIES + ":Boolean=false"})
public class PANApplianceManagerApi implements ApplianceManagerApi {

    private static final Logger LOG = Logger.getLogger(PANApplianceManagerApi.class);
    private Client client;
    private Config config;

    @ObjectClassDefinition
    @interface Config {
        @AttributeDefinition(required = false)
        boolean use_https() default true;

        @AttributeDefinition(min = "0", max = "65535", required = false, description = "The port to use when connecting to PAN instances. The value '0' indicates that a default port of '443' (or '80' if HTTPS is not enabled) should be used.")
        int port() default 0;

        @AttributeDefinition(min = "0", required = false)
        int max_threads() default 0;

        @AttributeDefinition(required = false, description = "The property name to use when setting the maximum thread count")
        String max_threads_property_name() default "com.sun.jersey.client.property.threadpoolSize";
    }

    @Activate
    void start(Config config) {
        this.config = config;

        this.client = ClientBuilder.newBuilder().property(config.max_threads_property_name(), config.max_threads())
                .register(new JAXBProvider()).sslContext(getSSLContext()).hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();
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

    @Deactivate
    void stop() {
        this.client.close();
    }

    private ShowOperations getShowOperations(ApplianceManagerConnectorElement mc) throws Exception {

        return new ShowOperations(mc.getIpAddress(), this.config.port(), this.config.use_https(), mc.getUsername(),
                mc.getPassword(), this.client);
    }

    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#createManagerDeviceApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement, org.osc.sdk.manager.element.VirtualSystemElement)
     */
    @Override
    public ManagerDeviceApi createManagerDeviceApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs)
            throws Exception {

        LOG.info(String.format("Creating Device Api for Panorama Manager : %s with ip : %s", mc.getName(),
                mc.getIpAddress()));
        ShowOperations showOperations = getShowOperations(mc);

        return new PANDeviceApi(mc, vs, showOperations);
    }

    /*
     * @see
     * org.osc.sdk.manager.api.ApplianceManagerApi#createManagerSecurityGroupInterfaceApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement, org.osc.sdk.manager.element.VirtualSystemElement)
     */
    @Override
    public ManagerSecurityGroupInterfaceApi createManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {

        LOG.info(String.format("Creating Security Group interface API for Panorama Manager : %s with ip : %s",
                mc.getName(), mc.getIpAddress()));
        //return PANManagerSecurityGroupInterfaceApi.create(mc,vs);
        return null;
    }

    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#createManagerSecurityGroupApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement, org.osc.sdk.manager.element.VirtualSystemElement)
     */
    @Override
    public ManagerSecurityGroupApi createManagerSecurityGroupApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {
        LOG.info(String.format("Creating Security Group API for Panorama Manager : %s with ip : %s", mc.getName(),
                mc.getIpAddress()));
        ShowOperations showOperations = getShowOperations(mc);
        return new PANManagerSecurityGroupApi(mc, vs, showOperations);
    }

    /*
     *
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#createManagerPolicyApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement)
     */
    @Override
    public ManagerPolicyApi createManagerPolicyApi(ApplianceManagerConnectorElement mc) throws Exception {

        LOG.info(String.format("Creating Policy API for Panorama Manager : %s with ip : %s", mc.getName(),
                mc.getIpAddress()));
        ShowOperations showOperations = getShowOperations(mc);
        return new PANManagerPolicyApi(mc, showOperations);
    }

    /*
     *
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#createManagerDomainApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement)
     */
    @Override
    public ManagerDomainApi createManagerDomainApi(ApplianceManagerConnectorElement mc) throws Exception {
        LOG.info(String.format("Creating Domain API for Panorama Manager : %s with ip : %s", mc.getName(),
                mc.getIpAddress()));
        return new PANManagerDomainApi(mc);
    }

    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#createManagerDeviceMemberApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement, org.osc.sdk.manager.element.VirtualSystemElement)
     */
    @Override
    public ManagerDeviceMemberApi createManagerDeviceMemberApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {

        LOG.info(String.format("Creating Device Member API for Panorama Manager : %s with ip : %s", mc.getName(),
                mc.getIpAddress()));
        return new PANManagerDeviceMemberApi(mc, vs);
    }

    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#getPublicKey(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement)
     */
    @Override
    public byte[] getPublicKey(ApplianceManagerConnectorElement mc) throws Exception {

        return null;
    }


    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#getManagerUrl(java.lang.String)
     */
    @Override
    public String getManagerUrl(String ipAddress) {
        return "https://" + ipAddress;
    }


    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#checkConnection(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement)
     */
    @Override
    public void checkConnection(ApplianceManagerConnectorElement mc) throws Exception {

        boolean connectionCheck = false;

        ShowOperations showOperations = getShowOperations(mc);
        connectionCheck = showOperations.checkConnection();
        if (connectionCheck == false) {
            String errorMessage = String.format("Failed to connect to Panorama Manager @ IP address : %s",
                    mc.getIpAddress());
            LOG.error(errorMessage);
            throw new Exception(errorMessage);
        }
    }


    /*
     * @see
     * org.osc.sdk.manager.api.ApplianceManagerApi#createManagerWebSocketNotificationApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement)
     */
    @Override
    public ManagerWebSocketNotificationApi createManagerWebSocketNotificationApi(ApplianceManagerConnectorElement mc)
            throws Exception {
        throw new UnsupportedOperationException("WebSocket Notification not implemented");
    }

    /*
     * @see
     * org.osc.sdk.manager.api.ApplianceManagerApi#createManagerCallbackNotificationApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement)
     */
    @Override
    public ManagerCallbackNotificationApi createManagerCallbackNotificationApi(ApplianceManagerConnectorElement mc)
            throws Exception {
        throw new UnsupportedOperationException("Manager does not support notification");
    }

    /*
     * @see org.osc.sdk.manager.api.ApplianceManagerApi#createIscJobNotificationApi(org.osc.sdk.manager.element.
     * ApplianceManagerConnectorElement, org.osc.sdk.manager.element.VirtualSystemElement)
     */
    @Override
    public IscJobNotificationApi createIscJobNotificationApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {

        return null;
    }


}
