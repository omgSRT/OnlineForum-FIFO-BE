package com.FA24SE088.OnlineForum.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
        //implements WebMvcConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowedOrigins("http://*/swagger-ui/swagger-ui/index.html","https://*/swagger-ui/swagger-ui/index.html")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//        .allowCredentials(true);
//    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
    //    config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Access-Control-Allow-Origin
        config.addAllowedHeader("*"); // Access-Control-Request-Headers
        config.addAllowedMethod("*"); // Access-Control-Request-Method

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

