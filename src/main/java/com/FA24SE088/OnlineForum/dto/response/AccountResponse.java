package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    String username;
    String handle;
    String email;
    String password;
    String bio;
    String gender;
    String address;
    String avatar;
    Date createdDate;
    String status;
    Role role;
}
