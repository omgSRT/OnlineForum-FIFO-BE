package com.FA24SE088.OnlineForum.configuration;

import jakarta.annotation.PostConstruct;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
public class SSLConfiguration {
    private static final String KEY_STORE_PATH = "src/main/resources/keystore.p12";
    private static final String PRIVATE_KEY_PATH = "privkey.pem";
    private static final String CERT_PATH = "cert.pem";
    private static final String CHAIN_PATH = "fullchain.pem";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEY_ALIAS = "keyAlias";

    @PostConstruct
    public void init() throws Exception {
        createKeyStore();
    }

    private void createKeyStore() throws Exception {
        URL keystoreUrl = getClass().getClassLoader().getResource("keystore.p12");
        if (keystoreUrl != null) {
            System.out.println("Keystore already exists at: " + KEY_STORE_PATH);
            return;
        }

        PrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
        X509Certificate certificate = loadCertificate(CERT_PATH);
        Certificate[] certificateChain = loadCertificateFullChain(CHAIN_PATH);

        // Create a KeyStore instance for PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        // Store the private key and certificate chain into the keystore
        keyStore.setKeyEntry(KEY_ALIAS, privateKey,
                KEYSTORE_PASSWORD.toCharArray(), certificateChain);

        // Write the keystore to the specified file
        try (FileOutputStream fos = new FileOutputStream(KEY_STORE_PATH)) {
            keyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
            System.out.println("Keystore created successfully at: " + KEY_STORE_PATH);
        } catch (IOException e) {
            System.err.println("Error writing keystore to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        InputStream privateKeyInputStream = getClass().getClassLoader().getResourceAsStream(privateKeyPath);
        if (privateKeyInputStream == null) {
            throw new IOException("Private key file not found: " + privateKeyPath);
        }

        String privateKeyContent = new String(privateKeyInputStream.readAllBytes());
        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");

        byte[] encodedKey = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private X509Certificate loadCertificate(String certPath) throws Exception {
        // Read certificate file content
        InputStream certInputStream = getClass().getClassLoader().getResourceAsStream(certPath);
        if (certInputStream == null) {
            throw new IOException("Certificate file not found: " + certPath);
        }

        String certContent = new String(certInputStream.readAllBytes());
        certContent = certContent.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "").replaceAll("\\s", "");

        byte[] encodedCert = Base64.getDecoder().decode(certContent);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert));
    }

    private Certificate[] loadCertificateFullChain(String chainPath) throws Exception {
        InputStream chainInputStream = getClass().getClassLoader().getResourceAsStream(chainPath);
        if (chainInputStream == null) {
            throw new IOException("Certificate chain file not found: " + chainPath);
        }

        String chainContent = new String(chainInputStream.readAllBytes());

        String[] certs = chainContent.split("-----END CERTIFICATE-----");

        List<Certificate> certificatesList = new ArrayList<>();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        for (String cert : certs) {
            if (cert.contains("-----BEGIN CERTIFICATE-----")) {
                cert = cert.replace("-----BEGIN CERTIFICATE-----", "")
                        .replace("-----END CERTIFICATE-----", "")
                        .replaceAll("\\s", "");
                byte[] encodedCert = Base64.getDecoder().decode(cert);
                certificatesList.add(certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)));
            }
        }

        if (certificatesList.isEmpty()) {
            throw new IOException("No valid certificates found in the chain file.");
        }

        return certificatesList.toArray(new Certificate[0]);
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        // Add the SSL connector to the Tomcat server
        factory.addConnectorCustomizers(connector -> {
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(8080);

            // Set up SSL properties using connector attributes
            URL keystoreUrl = getClass().getClassLoader().getResource("keystore.p12");
            if (keystoreUrl != null) {
                try (InputStream keystoreInputStream = getClass().getClassLoader().getResourceAsStream("keystore.p12")) {
                    // Create a temporary file to store the keystore content
                    File tempKeystoreFile = File.createTempFile("keystore", ".p12");
                    tempKeystoreFile.deleteOnExit();  // Ensure it gets cleaned up after the JVM exits

                    // Copy the InputStream content to the temporary file
                    try (FileOutputStream fos = new FileOutputStream(tempKeystoreFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = keystoreInputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }

                    // Set the keystore file path in the Tomcat connector
                    connector.setProperty("keystoreFile", tempKeystoreFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Keystore file not found in resources.");
                }
            } else {
                connector.setProperty("keystoreFile", new File("src/main/resources/keystore.p12").getAbsolutePath());
            }
            connector.setProperty("keystorePass", "password");
            connector.setProperty("keyAlias", "keyAlias");
            connector.setProperty("keystoreType", "PKCS12");
            connector.setProperty("clientAuth", "false");
            connector.setProperty("sslProtocol", "TLS");
        });

        return factory;
    }
}
