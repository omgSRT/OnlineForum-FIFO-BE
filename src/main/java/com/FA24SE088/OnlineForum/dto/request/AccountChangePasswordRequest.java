package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountChangePasswordRequest {
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character")
    @NotBlank(message = "Password Cannot Be Blank")
    String password;
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character")
    @NotBlank(message = "Confirm Password Cannot Be Blank")
    String confirmPassword;
}
