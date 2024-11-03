package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostUpdateRequest {
    @NotBlank(message = "Title Cannot Be Null")
    String title;
    @NotBlank(message = "Content Cannot Be Null")
    String content;
    Set<ImageRequest> imageUrlList;
    @Pattern(regexp = "^(http|https)://.*|^$", message = "URL Must Be Valid or Empty")
    String linkFile;
}
