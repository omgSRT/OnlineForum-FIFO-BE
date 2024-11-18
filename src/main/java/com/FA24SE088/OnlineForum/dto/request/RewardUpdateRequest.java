package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.enums.RewardStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardUpdateRequest {
    String name;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Avatar must be a valid URL")
    @Column(columnDefinition = "MEDIUMTEXT")
    String image;
    double price;
    RewardStatus status;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Avatar must be a valid URL")
    @Column(columnDefinition = "MEDIUMTEXT")
    String linkSourceCode;
}
