package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.enums.RoleAccount;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
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
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character")
    String password;
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character")
    String confirmPassword;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Avatar must be a valid URL")
    String avatar;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Cover image must be a valid URL")
    String coverImage;
    RoleAccount role;
    @Nullable
    List<String> categoryList_ForStaff;
}
