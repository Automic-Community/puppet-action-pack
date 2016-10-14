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
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;
import com.automic.puppet.util.ConsoleWriter;
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

    public static Client getClient(String skipCertValidation) throws AutomicException {
        ClientConfig config = new DefaultClientConfig();

        int connectionTimeOut = CommonUtil.getEnvParameter(Constants.ENV_CONNECTION_TIMEOUT, Constants.CONN_TIMEOUT);
        ConsoleWriter.writeln("Using Connection timeout as " + connectionTimeOut + " (ms)");
        int readTimeOut = CommonUtil.getEnvParameter(Constants.ENV_READ_TIMEOUT, Constants.READ_TIMEOUT);
        ConsoleWriter.writeln("Using Read timeout as " + readTimeOut + " (ms)");
        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);

        // to skip certificate validation
        if (CommonUtil.convert2Bool(skipCertValidation)) {
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, skipValidation());
        } else {
            String hostcertPath = CommonUtil.getEnvParameter(Constants.ENV_HOSTCERT, "");
            String hostprivkeyPath = CommonUtil.getEnvParameter(Constants.ENV_HOSTPRIVKEY, "");
            String localcacertPath = CommonUtil.getEnvParameter(Constants.ENV_LOCALCACERT, "");
            if (CommonUtil.checkNotEmpty(hostcertPath) && CommonUtil.checkNotEmpty(hostprivkeyPath)
                    && CommonUtil.checkNotEmpty(localcacertPath)) {
                config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        validateCertificates(hostcertPath, hostprivkeyPath, localcacertPath));
            }
        }
        return Client.create(config);
    }

    private static HTTPSProperties skipValidation() throws AutomicException {

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HTTPSProperties props = new HTTPSProperties(allHostsValid, sc);
            return props;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            ConsoleWriter.writeln(e);
            throw new AutomicException(ExceptionConstants.ERROR_SKIPPING_CERT);
        }
    }

    private static HTTPSProperties validateCertificates(String hostcertPath, String hostprivkeyPath,
            String localcacertPath) throws AutomicException {

        PuppetCertificate certs = new PuppetCertificate(hostcertPath, hostprivkeyPath, localcacertPath);
        return new HTTPSProperties(certs.hostnameVerifier(), certs.sslContext());
    }

}
