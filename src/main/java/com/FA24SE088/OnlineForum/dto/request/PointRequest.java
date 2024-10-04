package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PointRequest {
    @Min(value = 1, message = "Max point must be greater than or equal to 1")
    double maxPoint;
    @Min(value = 1, message = "Point per post must be greater than or equal to 1")
    double pointPerPost;
}
