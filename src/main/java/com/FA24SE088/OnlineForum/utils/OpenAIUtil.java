package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.dto.response.OpenAIResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenAIUtil {
    @Value("${openai.api.key}")
    String openaiKey;

    final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    final String EMBEDDING_OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    public boolean isRelated(String title, String content, String topicName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiKey);
        headers.set("Content-Type", "application/json");

        String prompt = String.format(
                "Given the title: \"%s\", content: \"%s\", and topic: \"%s\", please answer the following questions:" +
                        "\n1. Does the title have any general relationship, reference, or alignment with the topic \\\"%s\\\", even if the connection is indirect or not explicitly clear? Respond ONLY with \\\"true\\\" or \\\"false\\\"." +
                        "\n2. Does the content reference, relate to, or loosely imply the subject \"%s\" in any way, even indirectly? Respond ONLY with \"true\" or \"false\"." +
                        "\n3. Does the title and content loosely align or share a common theme, even if the relationship is indirect? Respond ONLY with \"true\" or \"false\"." +
                        "\nRespond as follows:\n" +
                        "Title and Topic Related: [true/false]\n" +
                        "Content and Topic Related: [true/false]\n" +
                        "Title and Content Related: [true/false]",
                title, content, topicName, topicName, topicName
        );


        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", new Object[]{
                        Map.of("role", "system", "content",
                                "You are an assistant that evaluates relationships between a title, content, and a topic. " +
                                        "Respond with structured answers exactly as instructed. " +
                                        "Minimal, informal references to the topic are acceptable if they are clearly related."
                        ),
                        Map.of("role", "user", "content", prompt)
                },
                "max_tokens", 100,
                "temperature", 0
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        OpenAIResponse openAIResponse = null;
        try {
            openAIResponse = new ObjectMapper().readValue(response.getBody(), OpenAIResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (openAIResponse.getChoices() != null && !openAIResponse.getChoices().isEmpty()) {
            String result = openAIResponse.getChoices().get(0).getMessage().getContent().trim().toLowerCase();

            System.out.println(result);

            boolean titleTopicRelated = false;
            boolean contentTopicRelated = false;
            boolean contentTitleRelated = false;

            for (String line : result.split("\n")) {
                if (line.startsWith("title and topic related:")) {
                    titleTopicRelated = Boolean.parseBoolean(line.split(":")[1].trim());
                } else if (line.startsWith("content and topic related:")) {
                    contentTopicRelated = Boolean.parseBoolean(line.split(":")[1].trim());
                } else if (line.startsWith("title and content related:")) {
                    contentTitleRelated = Boolean.parseBoolean(line.split(":")[1].trim());
                }
            }

            var percentageTitleTopic = calculateExactWordContainmentPercentage(title, topicName);
            var percentageContentTopic = calculateExactWordContainmentPercentage(content, topicName);
            var percentageTitleContent = calculateExactWordContainmentPercentage(content, title);

            System.out.println(percentageTitleTopic);
            System.out.println(percentageContentTopic);
            System.out.println(percentageTitleContent);

            if (!titleTopicRelated) {
                if(percentageTitleTopic < 40) {
                    throw new AppException(ErrorCode.TITLE_NOT_RELATED_TO_TOPIC);
                }
            }
            if (!contentTopicRelated) {
                if(percentageContentTopic < 40){
                    throw new AppException(ErrorCode.CONTENT_NOT_RELATED_TO_TOPIC);
                }
            }
            if (!contentTitleRelated) {
                if(percentageTitleContent < 40){
                    throw new AppException(ErrorCode.CONTENT_NOT_RELATED_TO_TITLE);
                }
            }

            return true;
        }
        return false;
    }

    private double calculateExactWordContainmentPercentage(String source, String target) {
        if (source == null || target == null || source.isEmpty() || target.isEmpty()) {
            return 0;
        }

        String[] sourceWords = source.toLowerCase().split("\\s+");
        String[] targetWords = target.toLowerCase().split("\\s+");

        int matchCount = 0;

        for (String targetWord : targetWords) {
            for (String sourceWord : sourceWords) {
                if (sourceWord.equals(targetWord)) {
                    matchCount++;
                }
            }
        }

        return (double) (matchCount * 100) / sourceWords.length;
    }

    public boolean isRelatedUsingVector(String title, String content, String topicName) {
        Double[] titleEmbedding = getEmbeddings(title);
        Double[] contentEmbedding = getEmbeddings(content);
        Double[] topicEmbedding = getEmbeddings(topicName);

        double titleTopicSimilarity = cosineSimilarity(titleEmbedding, topicEmbedding);
        double contentTopicSimilarity = cosineSimilarity(contentEmbedding, topicEmbedding);
        double titleContentSimilarity = cosineSimilarity(titleEmbedding, contentEmbedding);

        double threshold = 0.6;

        if (titleTopicSimilarity < threshold) {
            throw new AppException(ErrorCode.TITLE_NOT_RELATED_TO_TOPIC);
        }
        if (contentTopicSimilarity < threshold) {
            throw new AppException(ErrorCode.CONTENT_NOT_RELATED_TO_TOPIC);
        }
        if (titleContentSimilarity < threshold) {
            throw new AppException(ErrorCode.CONTENT_NOT_RELATED_TO_TITLE);
        }

        return true;
    }

    //formula: (A * B)/(|A| * |B|)
    private double cosineSimilarity(Double[] vectorA, Double[] vectorB) {
        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += Math.pow(vectorA[i], 2);
            magnitudeB += Math.pow(vectorB[i], 2);
        }

        double denominator = Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB);
        if (denominator == 0) {
            return 0.0;
        }

        return dotProduct / denominator;
    }

    private Double[] getEmbeddings(String text) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = Map.of(
                "model", "text-embedding-ada-002",  // You can use a different embedding model
                "input", text
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                EMBEDDING_OPENAI_API_URL,  // Endpoint for embeddings
                HttpMethod.POST,
                entity,
                String.class
        );

        // Parse the response to get the embedding vector
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Use TypeReference to deserialize the response into a specific type
            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });

            // Extract the 'data' field as a List of Maps
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");

            // Extract the 'embedding' field as a List of Doubles from the first data entry
            List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");

            // Convert the List<Double> to Double[] and return
            return embeddingList.toArray(new Double[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching embedding", e);
        }
    }

}
