package com.automic.puppet.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;

/**
 * CertificatesManagement holds certificates for connecting to an HTTPS-secured JFrog instance with client/server
 * authentication.
 */
public class CertificatesManagement {
    private final SSLContext sslContext;

    CertificatesManagement(String keyStoreLoc, String password) throws AutomicException {
        try {
            this.sslContext = setSSLSocketContext(keyStoreLoc, password);
        } catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException
                | IOException e) {
            throw new AutomicException(ExceptionConstants.SSLCONTEXT_ERROR + e.getMessage(), e);
        }
    }

    private SSLContext setSSLSocketContext(String keyStoreName, String password) throws UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException, AutomicException {
        KeyStore ks = getKeyStore(keyStoreName, password);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(ks, password.toCharArray());

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

        return context;
    }

    private KeyStore getKeyStore(String keyStoreName, String password) throws IOException, AutomicException {
        KeyStore ks = null;
        try (FileInputStream fis = new java.io.FileInputStream(keyStoreName)) {
            ks = KeyStore.getInstance("JKS");
            char[] passwordArray = password.toCharArray();
            ks.load(fis, passwordArray);
            fis.close();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new AutomicException(ExceptionConstants.INVALID_KEYSTORE, e);
        }
        return ks;
    }

    /**
     * Method to get the instance of {@link SSLContext} for jfrog connection
     *
     * @return
     */
    public SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Method to get the instance of {@link HostnameVerifier}
     *
     * @return
     */
    public HostnameVerifier hostnameVerifier() {
        return SSLConnectionSocketFactory.getDefaultHostnameVerifier();
    }

}
