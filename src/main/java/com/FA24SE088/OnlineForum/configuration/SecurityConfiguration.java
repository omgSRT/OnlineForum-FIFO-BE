package com.FA24SE088.OnlineForum.configuration;

import com.FA24SE088.OnlineForum.service.CustomOAuth2UserService;
import com.FA24SE088.OnlineForum.utils.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    //    private final String[] PUBLIC_ENDPOINTS_POST = {"/authenticate/login", "/authenticate/introspect", "/authenticate/logout",
//            "/authenticate/refresh", "/email/send", "/authenticate/sign-up",
//            "/daily-point/create", "/notification/create", "/notification/change/status",
//            "/account/create", "/authenticate/sign-up", "/authenticate/resend-otp", "/authenticate/verify-email",
//            "/authenticate/forget-password"};
    private final String[] PUBLIC_ENDPOINTS_POST = {"/authenticate/**", "/email/send",
            "/daily-point/create", "/notification/create", "/notification/change/status", "/account/create", "/transaction/create"};
    private final String[] PUBLIC_ENDPOINTS_GET = {"/swagger-ui/**", "/v3/api-docs/**",
            "/swagger-resources/**","authenticate/**"
    };
    private final String[] PUBLIC_ENDPOINTS_PUT = {
            "/authenticate/change-password"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    @Autowired
    OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(request ->
                        request
                                .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS_GET).permitAll()
                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS_POST).permitAll()
                                .requestMatchers(HttpMethod.PUT, PUBLIC_ENDPOINTS_PUT).permitAll()
                                .requestMatchers("/ws/**", "/websocket/**", "websocket/app/**", "/socket.io/**",
                                        "/payment/**","http://localhost:8080/login","https://fifoforumonline.click/login").permitAll()
                                //.requestMatchers("/**").permitAll()
                                .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)

        ;

        http.oauth2Login(oauth2Login ->
                oauth2Login
                        .defaultSuccessUrl("/authenticate/callback", true)
                        .failureUrl("/login?error=true")
                        .successHandler(oAuth2LoginSuccessHandler)
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)) // Service để xử lý thông tin user từ Google
        );

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
