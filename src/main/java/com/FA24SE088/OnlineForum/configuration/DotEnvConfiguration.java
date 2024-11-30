package com.FA24SE088.OnlineForum.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotEnvConfiguration {
    @PostConstruct
    public void loadEnvVars() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .filename("local.env")
                .load();
        String apiKey = dotenv.get("OPENAI_API_KEY");
        System.setProperty("OPENAI_API_KEY", apiKey);
    }
}
