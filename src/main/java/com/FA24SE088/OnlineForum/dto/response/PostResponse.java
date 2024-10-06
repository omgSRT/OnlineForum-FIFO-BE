package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Topic;
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
public class PostResponse {
    UUID postId;
    String title;
    String content;
    Date createdDate;
    Date lastModifiedDate;
    String status;
    @JsonIgnoreProperties(value = { "password", "bio", "gender", "address", "createdDate", "status", "role" })
    Account account;
    @JsonIgnoreProperties(value = { "category.account" })
    Topic topic;
}
