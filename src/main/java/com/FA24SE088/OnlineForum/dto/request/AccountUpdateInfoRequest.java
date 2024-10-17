package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountUpdateInfoRequest {
    String oldPassword;
    String newPass;
    String confirmPassword;
    String avatar;
    String coverImage;
}
