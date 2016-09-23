package com.automic.puppet.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.automic.puppet.constants.Constants;
import com.automic.puppet.exception.AutomicException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * This class is used to instantiate HTTP Client required by action(s).
 *
 */
public final class HttpClientConfig {

    private HttpClientConfig() {
    }

    public static Client getClient(String protocol, int connectionTimeOut, int readTimeOut) throws AutomicException {
        Client client;

        ClientConfig config = new DefaultClientConfig();

        config.getClasses().add(com.sun.jersey.multipart.impl.MultiPartWriter.class);
        try {
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, skipCertValidation());
            System.out.println("executed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);

        if (Constants.HTTPS.equalsIgnoreCase(protocol)) {
            // https code goes here
        }
        client = Client.create(config);

        return client;
    }

    /**
     * Method to create HTTP client config with user defined ssl context
     *
     * @param keyStore
     *            kesytore file
     * @param password
     *            password to keystore file
     * @param connectionTimeOut
     *            connection timeout
     * @param readTimeOut
     *            read timeout
     * @return
     * @throws AutomicException
     */
    public static ClientConfig getClientConfig(String keyStore, String password, int connectionTimeOut, int readTimeOut)
            throws AutomicException {
        ClientConfig config = new DefaultClientConfig();

        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);

        if (keyStore != null && password != null) {
            CertificatesManagement acm = new CertificatesManagement(keyStore, password);
            HTTPSProperties props = new HTTPSProperties(acm.hostnameVerifier(), acm.getSslContext());
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, props);

        }

        return config;
    }

    /**
     * Method to create HTTP client config without user defined ssl context
     *
     * @param connectionTimeOut
     * @param readTimeOut
     * @return
     * @throws AutomicException
     */
    public static ClientConfig getClientConfig(int connectionTimeOut, int readTimeOut) throws AutomicException {

        return getClientConfig(null, null, connectionTimeOut, readTimeOut);
    }

    public static HTTPSProperties skipCertValidation() throws NoSuchAlgorithmException, KeyManagementException {

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HTTPSProperties props = new HTTPSProperties(allHostsValid, sc);
        return props;
    }

}
