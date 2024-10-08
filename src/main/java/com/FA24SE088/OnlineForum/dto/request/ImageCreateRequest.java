package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageCreateRequest {
    @NotBlank(message = "URL Cannot Be Null")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL Must Be Valid")
    List<String> urls;
    UUID postId;
}
