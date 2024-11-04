package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DraftUpdateRequest {
    String title;
    String content;
    UUID topicId;
    UUID tagId;
    Set<ImageRequest> imageUrlList;
    @Pattern(regexp = "^(http|https)://.*|^$", message = "URL Must Be Valid or Empty")
    String linkFile;
}
