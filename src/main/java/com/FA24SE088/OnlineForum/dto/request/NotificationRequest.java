package com.FA24SE088.OnlineForum.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String title;
    String message;
//    boolean isRead;
    UUID accountId;
}
