package com.FA24SE088.OnlineForum.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackRequest {
    @NotBlank
    String title;
    @NotBlank
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;
}

