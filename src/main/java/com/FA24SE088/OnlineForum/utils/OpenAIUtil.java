package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.dto.response.OpenAIResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenAIUtil {
    @Value("${openai.api.key}")
    String openaiKey;

    final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public boolean isRelated(String title, String content, String topicName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiKey);
        headers.set("Content-Type", "application/json");

        String prompt = String.format(
                "Given the title: \"%s\" and the content: \"%s\", please answer two questions:" +
                        "\n1. Is the title and content related to the topic \"%s\"? Respond ONLY with \"true\" or \"false\"." +
                        "\n2. Is the content related to the title? Respond ONLY with \"true\" or \"false\"." +
                        "\nRespond as follows:\n" +
                        "Title and Topic Related: [true/false]\n" +
                        "Content and Title Related: [true/false]",
                title, content, topicName
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You are an assistant that responds with structured answers. Provide answers exactly as instructed in the user's prompt."),
                        Map.of("role", "user", "content", prompt)
                },
                "max_tokens", 50,
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

                boolean titleTopicRelated = false;
                boolean contentTitleRelated = false;

                for (String line : result.split("\n")) {
                    if (line.startsWith("title and topic related:")) {
                        titleTopicRelated = Boolean.parseBoolean(line.split(":")[1].trim());
                    } else if (line.startsWith("content and title related:")) {
                        contentTitleRelated = Boolean.parseBoolean(line.split(":")[1].trim());
                    }
                }

                if(!titleTopicRelated){
                    throw new AppException(ErrorCode.TITLE_AND_CONTENT_NOT_RELATED_TO_TOPIC);
                }
                if(!contentTitleRelated){
                    throw new AppException(ErrorCode.CONTENT_NOT_RELATED_TO_TITLE);
                }

                return true;
            }
            return false;
    }
}