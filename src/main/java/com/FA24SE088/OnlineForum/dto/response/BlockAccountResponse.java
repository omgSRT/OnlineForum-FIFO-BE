package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockAccountResponse {
    UUID followId;
    String message;
    @JsonIgnoreProperties(value = { "password", "email", "coverImage", "createdDate", "status", "role" })
    Account blocker;
    @JsonIgnoreProperties(value = { "password", "email", "coverImage", "createdDate", "status", "role" })
    Account blocked;
}