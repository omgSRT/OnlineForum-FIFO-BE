package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentUpdateRequest {
    @NotBlank(message = "Content Cannot Be Null")
    String content;
}
