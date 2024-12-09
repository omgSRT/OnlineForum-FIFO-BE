package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PopularTopicResponse {
    UUID topicId;
    String name;
    String imageUrl;
    int postAmount;
    int upvoteAmount;
    int commentAmount;
    int viewAmount;
    @JsonIgnoreProperties(value = {"account"})
    Category category;
}
