package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchEverythingResponse {
    @JsonIgnoreProperties(value = { "password", "email", "bio", "gender", "address", "createdDate", "status", "role" })
    List<Account> accountList;
    @JsonIgnoreProperties(value = { "account" })
    List<Category> categoryList;
    @JsonIgnoreProperties(value = { "category" })
    List<Topic> topicList;
    @JsonIgnoreProperties(value = { "account", "topic", "tag" })
    List<Post> postList;
}
