package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostViewResponse {
    UUID postViewId;
    Date viewedDate;
    @JsonIgnoreProperties(value = { "password", "email", "bio", "gender", "address", "createdDate", "status", "role" })
    Account account;
    @JsonIgnoreProperties(value = { "account", "tag", "status", "topic" })
    Post post;
}
