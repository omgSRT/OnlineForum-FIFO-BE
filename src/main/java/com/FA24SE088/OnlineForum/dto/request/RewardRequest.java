package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardRequest {
    String name;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Avatar must be a valid URL")
    String image;
    @Size(min = 100, message = "price at least 100 points ")
    double price;
    String type;
    String status;
    List<SectionRequest> sectionList;
}
