package com.FA24SE088.OnlineForum.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class DotEnvConfiguration {
    @PostConstruct
    public void loadEnvVars() {
        InputStream envStream = getClass().getClassLoader().getResourceAsStream("local.env");
        if (envStream == null) {
            throw new IllegalArgumentException("local.env not found in classpath");
        }

        Dotenv dotenv = Dotenv.configure()
                .filename("local.env") // The filename is still required
                .load();

        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("OPENAI_API_KEY not found in local.env");
        }

        System.setProperty("OPENAI_API_KEY", apiKey);
    }
}
