package com.FA24SE088.OnlineForum.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AkismetUtil {
    @Value("${server.protocol-method}")
    private String protocolMethod;

    @Value("${akismet.api.key}")
    private String apiKey;

    @Value("${akismet.api.url}")
    private String apiUrl;

    public boolean isCommentSpam(String userIP, String userAgent,
                                 String username, String content){
        RestTemplate restTemplate = new RestTemplate();

        String blogUrl;
        if(protocolMethod.equalsIgnoreCase("https")){
            blogUrl = "https://fifoforumonline.click";
        }
        else{
            blogUrl = "http://localhost:8080";
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("key", apiKey);
        formData.add("blog", blogUrl);
        formData.add("user_ip", userIP);
        formData.add("user_agent", userAgent);
        formData.add("comment_author", username);
        formData.add("comment_content", content);

        System.out.println(formData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        // Send the request
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST, // Use POST, not GET
                request,
                String.class
        );

        System.out.println(response.getStatusCode());
        System.out.println(response.getHeaders());
        System.out.println(response.getBody());

        return "true".equalsIgnoreCase((String) response.getBody());
    }
}