package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCreateRequest {
    @NotBlank(message = "Title Cannot Be Null")
    String title;
    @NotBlank(message = "Content Cannot Be Null")
    String content;
    UUID accountId;
    UUID topicId;
    UUID tagId;
}
