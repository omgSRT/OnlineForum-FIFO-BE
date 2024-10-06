package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageRequest {
    @NotBlank(message = "URL Cannot Be Null")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL Must Be Valid")
    String url;
}
