package com.FA24SE088.OnlineForum.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyPointRequest {
    UUID accountId;
    UUID postId;
    UUID typeBonusId = null;
}
