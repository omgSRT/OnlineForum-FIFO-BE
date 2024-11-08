package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Topic;
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
public class CategoryGetAllResponse {
    UUID categoryId;
    String name;
    String description;
    String image;
    @JsonIgnoreProperties(value = { "category", "postList" })
    List<Topic> topicListByCategory;
    int upvoteCount;
    int commentCount;
    int viewCount;
}
