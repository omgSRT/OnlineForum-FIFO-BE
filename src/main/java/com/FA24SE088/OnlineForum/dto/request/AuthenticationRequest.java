package com.FA24SE088.OnlineForum.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Data
//@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @Size(min = 8, max = 20, message = "Username must be 8-20 characters long")
    private String username;
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters long")
    private String password;
}
