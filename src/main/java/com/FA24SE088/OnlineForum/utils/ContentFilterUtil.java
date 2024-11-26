package com.FA24SE088.OnlineForum.utils;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v2.*;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Component
public class ContentFilterUtil {
    private static final String CREDENTIALS_FILE = "serviceAccountCloudGoogleKey.json";

    private CredentialsProvider getCredentialsProvider() throws IOException {
        try (InputStream credentialsStream =
                     ContentFilterUtil.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE)) {

            if (credentialsStream == null) {
                throw new IllegalArgumentException("Service account key file not found in resources folder.");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            return FixedCredentialsProvider.create(credentials);
        } catch (IOException e) {
            throw new IOException("Error reading the credentials file.", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Service account key file is missing or invalid.", e);
        }
    }

    private ImageAnnotatorClient getImageAnnotatorClient() throws IOException {
        return ImageAnnotatorClient.create(ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(getCredentialsProvider())
                .build());
    }

    private LanguageServiceClient getLanguageServiceClient() throws IOException {
        return LanguageServiceClient.create(LanguageServiceSettings.newBuilder()
                .setCredentialsProvider(getCredentialsProvider())
                .build());
    }

    public boolean isImageSafe(String imageUrl) throws Exception {
        try (ImageAnnotatorClient vision = getImageAnnotatorClient()) {
            ByteString imgBytes = downloadImage(imageUrl);

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder()
                            .addFeatures(feat)
                            .setImage(img)
                            .build();

            AnnotateImageResponse response = vision.batchAnnotateImages(
                    java.util.Collections.singletonList(request)).getResponses(0);

            if (response.hasError()) {
                throw new Exception("Error analyzing image: " + response.getError().getMessage());
            }

            SafeSearchAnnotation annotation = response.getSafeSearchAnnotation();
            return annotation.getAdult().getNumber() < Likelihood.POSSIBLE_VALUE &&
                    annotation.getViolence().getNumber() < Likelihood.POSSIBLE_VALUE &&
                    annotation.getRacy().getNumber() < Likelihood.POSSIBLE_VALUE;
        }
    }

    public boolean isTextSafe(String text) throws Exception {
        try (LanguageServiceClient language = getLanguageServiceClient()) {
            Document doc = Document.newBuilder()
                    .setContent(text)
                    .setType(Document.Type.PLAIN_TEXT)
                    .build();

            Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
            float score = sentiment.getScore();

            return !(score < -0.5);
        }
    }


    public boolean areContentsSafe(List<String> imageUrls, String title, String description) throws Exception {
        boolean areImagesSafe = true;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                if (!isImageSafe(imageUrl)) {
                    areImagesSafe = false;
                    break;
                }
            }
        }

        boolean isTitleSafe = title == null || isTextSafe(title);
        boolean isDescriptionSafe = description == null || isTextSafe(description);

        return areImagesSafe && isTitleSafe && isDescriptionSafe;
    }


    private static ByteString downloadImage(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);

        try (InputStream inputStream = connection.getInputStream()) {
            return ByteString.readFrom(inputStream);
        } finally {
            connection.disconnect();
        }
    }
}
