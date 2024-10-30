package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.enums.RoleAccount;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountRequest {
    @Size(min = 8, max = 20, message = "User must be least 8-20 character")
    String username;
    @Email
    String email;
    String bio;
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone invalid, must be 10-15 number")
    String phone;
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character")
    String password;
    String confirmPassword;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Avatar must be a valid URL")
    String avatar;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Cover image must be a valid URL")
    String coverImage;
    RoleAccount role;
    List<String> categoryList;
}
