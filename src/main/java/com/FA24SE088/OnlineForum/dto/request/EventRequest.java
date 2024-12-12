package com.FA24SE088.OnlineForum.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequest {
    String title;
    Date startDate;
    Date endDate;
    String location;
    @Column(columnDefinition = "MEDIUMTEXT")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL Must Be Valid")
    @NotBlank
    String image;
    @Column(columnDefinition = "MEDIUMTEXT")
    @NotBlank
    String content;
    @Column(columnDefinition = "MEDIUMTEXT")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL Must Be Valid")
    @NotBlank
    String link;
}
