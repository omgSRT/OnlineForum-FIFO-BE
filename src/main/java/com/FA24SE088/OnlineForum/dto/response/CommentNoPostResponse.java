package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Comment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentNoPostResponse {
    UUID commentId;
    String content;
    @JsonIgnoreProperties(value = { "password", "email", "bio", "gender", "address", "createdDate", "status", "role" })
    Account account;
    List<Comment> replies;
}
