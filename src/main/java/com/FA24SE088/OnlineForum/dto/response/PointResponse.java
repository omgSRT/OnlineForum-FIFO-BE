package com.FA24SE088.OnlineForum.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PointResponse {
    UUID pointId;
    double maxPoint;
    double pointPerPost;
    double pointCostPerDownload;
    double pointEarnedPerDownload;
    long reportThresholdForAutoDelete;
}
