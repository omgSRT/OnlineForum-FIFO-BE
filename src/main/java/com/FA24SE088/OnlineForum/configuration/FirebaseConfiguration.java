package com.FA24SE088.OnlineForum.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfiguration {
    final static String bucketName = "image-description-detail.appspot.com";

    @EventListener
    public void initFirebaseApp(ApplicationReadyEvent event) {
        try {
//            String filePath = "D:/Capstone/ConstructionDrawingManagement/src/main/resources/serviceAccountKey.json";
//            FileInputStream serviceAccount = new FileInputStream(filePath);
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountFirebaseKey.json");

            // Configure FirebaseOptions with the provided credentials and Storage bucket
            assert serviceAccount != null;
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(bucketName)  // Specify Firebase Storage bucket
                    .build();

            // Initialize FirebaseApp with the provided options
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
