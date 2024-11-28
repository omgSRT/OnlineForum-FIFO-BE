package com.FA24SE088.OnlineForum.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoDResponse {
    long accountAmount;
    double accountGrowthRate;
    long postAmount;
    double postGrowthRate;
    long activityAmount;
    double activityGrowthRate;
    long depositAmount;
    double depositGrowthRate;
}
