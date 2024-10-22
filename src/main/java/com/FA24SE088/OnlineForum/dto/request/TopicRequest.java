package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopicRequest {
    @NotBlank(message = "Name Cannot Be Null")
    String name;
    UUID categoryId;
    @Pattern(regexp = "^(http|https)://.*$", message = "URL Must Be Valid")
    String imageUrl;
}
