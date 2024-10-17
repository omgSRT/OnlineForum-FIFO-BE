package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Role;
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
    @Size(min = 8, max = 20, message = "User must be least 8-20 character" )
    String username;
    String email;
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character" )
    String password;
    String confirmPassword;
    String avatar;
    String coverImage;
    String roleName;
    List<String> categoryList;
}
