package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Comment;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostGetByIdResponse {
    UUID postId;
    String title;
    String content;
    Date createdDate;
    Date lastModifiedDate;
    String status;
    Account account;
    @JsonIgnoreProperties(value = { "category.account" })
    Topic topic;
    @JsonIgnoreProperties(value = {
            "account.password",
            "account.bio",
            "account.gender",
            "account.address",
            "account.createdDate",
            "account.status",
            "account.role"
    })
    List<Comment> commentList;
}
