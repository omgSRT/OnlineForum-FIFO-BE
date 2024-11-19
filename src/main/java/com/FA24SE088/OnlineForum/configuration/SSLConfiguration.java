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
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SSLConfiguration {
    private static final String KEY_STORE_PATH = "src/main/resources/keystore.p12";

    @PostConstruct
    public void init() throws Exception {
        createKeyStore();
    }

    private void createKeyStore() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("serverCertification.json");
        if (inputStream == null) {
            throw new IOException("Server Certificate JSON file not found.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputStream);

        String privateKeyStr = root.get("privateKey").asText();
        String certificateStr = root.get("certificate").asText();

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replace("\n", ""));
        byte[] certificateBytes = Base64.getDecoder().decode(certificateStr.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "").replace("\n", ""));

        // Load the private key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        // Load the certificate
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));

        // Create a KeyStore and load the private key and certificate
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null); // Initialize empty keystore

        // Set the private key and certificate in the keystore
        keyStore.setKeyEntry("keyAlias", privateKey, "password".toCharArray(),
                new java.security.cert.Certificate[]{certificate});

        // Save the keystore to a file (in .p12 format)
        try (FileOutputStream fos = new FileOutputStream(KEY_STORE_PATH)) {
            keyStore.store(fos, "password".toCharArray());
        }
    }
}
