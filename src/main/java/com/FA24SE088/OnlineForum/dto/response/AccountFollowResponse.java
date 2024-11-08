package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountFollowResponse {
    UUID accountId;
    String username;
    String email;
    String handle;
    String avatar;
    String phone;
    String bio;
    String coverImage;
    Date createdDate;
    String status;
    Role role;
    long countFollowers;
}
