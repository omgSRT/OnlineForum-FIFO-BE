package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopFollowedAccountResponse {
    Account account;
    long countFollowers;
}
