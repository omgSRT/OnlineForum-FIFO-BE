package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String title;
    String message;
    boolean isRead;
    @Nullable
    UUID accountId;
}
