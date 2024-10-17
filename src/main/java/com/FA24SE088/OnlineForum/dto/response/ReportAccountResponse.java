package com.FA24SE088.OnlineForum.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportAccountResponse {
    private UUID reportAccountId;
    private String reason;
    private Date reportTime;
    private String status;
    private UUID reporterId;
    private UUID reportedId;
}


