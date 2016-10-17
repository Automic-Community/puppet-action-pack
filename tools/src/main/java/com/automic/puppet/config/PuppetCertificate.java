package com.automic.puppet.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.ConsoleWriter;

/**
 * This class generates the SSL Context using puppet certificates.
 * 
 */
public class PuppetCertificate {

    private static final char[] KEY_STORE_PASSWORD = "puppet!!11!!one!".toCharArray();

    private File caCertPath;
    private File clientKeyPath;
    private File clientCertPath;

    private SSLContext sslContext;

    public PuppetCertificate(String hostcertPath, String hostprivkeyPath, String localcacertPath)
            throws AutomicException {

        ConsoleWriter.writeln("Host Certificate :" + hostcertPath);
        ConsoleWriter.writeln("key :" + hostprivkeyPath);
        ConsoleWriter.writeln("CA Certificate :" + localcacertPath);

        clientCertPath = new File(hostcertPath);
        clientKeyPath = new File(hostprivkeyPath);
        caCertPath = new File(localcacertPath);

        if (!caCertPath.exists() || !clientKeyPath.exists() || !clientCertPath.exists()) {
            throw new AutomicException(ExceptionConstants.PUPPET_CERTIFICATE_MISSING);
        }
        generateCertificates();
    }

    /**
     * Method to generate certificates for connection to Docker system.
     * 
     * @throws DockerException
     */
    private void generateCertificates() throws AutomicException {

        FileReader reader = null;
        try {
            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final Certificate caCert = cf.generateCertificate(new FileInputStream(caCertPath));
            final Certificate clientCert = cf.generateCertificate(new FileInputStream(clientCertPath));

            reader = new FileReader(clientKeyPath);

            @SuppressWarnings("resource")
            final PEMKeyPair clientKeyPair = (PEMKeyPair) new PEMParser(reader).readObject();

            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(clientKeyPair.getPrivateKeyInfo().getEncoded());
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            final PrivateKey clientKey = kf.generatePrivate(spec);

            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setEntry("ca", new KeyStore.TrustedCertificateEntry(caCert), null);

            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("client", clientCert);
            keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD, new Certificate[] { clientCert });

            this.sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, null)
                    .loadKeyMaterial(keyStore, KEY_STORE_PASSWORD).useProtocol("TLS").build();

        } catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException
                | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            ConsoleWriter.writeln(e);
            throw new AutomicException(ExceptionConstants.INVALID_PUPPET_CERITIFCATES);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }
    }

    /**
     * Method to get the instance of {@link SSLContext} for Docker connection
     * 
     * @return
     */
    public SSLContext sslContext() {

        return this.sslContext;
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
