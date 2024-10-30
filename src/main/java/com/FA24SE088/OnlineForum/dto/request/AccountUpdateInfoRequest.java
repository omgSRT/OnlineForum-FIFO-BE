package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.Valid;
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
public class AccountUpdateInfoRequest {
    String oldPassword;
    @Size(min = 8, max = 20, message = "Pass must be least 8-20 character")
    String newPass;
    String bio;
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone invalid, must be 10-15 number")
    String phone;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Avatar must be a valid URL")
    String avatar;
    @Pattern(regexp = "^(http|https)://.*$|^$", message = "Cover image must be a valid URL")
    String coverImage;
}
