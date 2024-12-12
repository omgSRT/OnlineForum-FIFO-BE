package com.FA24SE088.OnlineForum.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardRequest {
    String name;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Image must be a valid URL")
    @Column(columnDefinition = "MEDIUMTEXT")
    @NotBlank
    String image;

    double price;

    @Column(columnDefinition = "MEDIUMTEXT")
    @NotBlank
    String description;

    @Column(columnDefinition = "MEDIUMTEXT")
    @NotBlank
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Link SourceCode must be a valid URL")
    String linkSourceCode;

}
