package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Post;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {
    UUID feedbackId;
    String title;
    String content;
    String status;
    UUID postId;
    String postTitle;
    String postContent;
    Date postCreatedDate;
    Date postLastModifiedDate;
    String postStatus;
}
