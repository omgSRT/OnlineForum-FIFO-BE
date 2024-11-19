package com.FA24SE088.OnlineForum.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SSLConfiguration {
    private static final String KEY_STORE_PATH = "src/main/resources/keystore.p12";
    private static final String PRIVATE_KEY_PATH = "src/main/resources/privkey.pem";
    private static final String CERT_PATH = "src/main/resources/cert.pem";
    private static final String CHAIN_PATH = "src/main/resources/chain.pem";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEY_ALIAS = "keyAlias";

    @PostConstruct
    public void init() throws Exception {
        createKeyStore();
    }

    private void createKeyStore() throws Exception {
        PrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
        X509Certificate certificate = loadCertificate(CERT_PATH);
        Certificate[] certificateChain = loadCertificateChain(CHAIN_PATH);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        keyStore.setKeyEntry(KEY_ALIAS, privateKey, KEYSTORE_PASSWORD.toCharArray(), certificateChain);

        try (FileOutputStream fos = new FileOutputStream(KEY_STORE_PATH)) {
            keyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
        }
    }

    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        String privateKeyContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(privateKeyPath)));
        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");

        byte[] encodedKey = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private X509Certificate loadCertificate(String certPath) throws Exception {
        String certContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(certPath)));
        certContent = certContent.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "").replaceAll("\\s", "");

        byte[] encodedCert = Base64.getDecoder().decode(certContent);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert));
    }

    private Certificate[] loadCertificateChain(String chainPath) throws Exception {
        String chainContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(chainPath)));
        String[] certs = chainContent.split("-----END CERTIFICATE-----");

        Certificate[] certificates = new Certificate[certs.length];
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        for (int i = 0; i < certs.length; i++) {
            if (certs[i].contains("-----BEGIN CERTIFICATE-----")) {
                String cert = certs[i].replace("-----BEGIN CERTIFICATE-----", "")
                        .replace("-----END CERTIFICATE-----", "").replaceAll("\\s", "");
                byte[] encodedCert = Base64.getDecoder().decode(cert);
                certificates[i] = certFactory.generateCertificate(new ByteArrayInputStream(encodedCert));
            }
        }
        return certificates;
    }
}
