package com.FA24SE088.OnlineForum.dto.requests;

import com.FA24SE088.OnlineForum.entity.Role;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountRequest {
    @Size(min = 8, max = 20, message = "User must be least 8-20 character" )
    String username;
    String handle;
    String email;
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character" )
    String password;
    String bio;
    String gender;
    String address;
    String avatar;
    String roleName;
}
