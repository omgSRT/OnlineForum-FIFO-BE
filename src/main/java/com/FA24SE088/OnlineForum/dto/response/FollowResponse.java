package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FollowResponse {
    UUID followId;
    String status;
    @JsonIgnoreProperties(value = { "password", "email", "coverImage", "createdDate", "status", "role" })
    Account followee;
    @JsonIgnoreProperties(value = { "password", "email", "coverImage", "createdDate", "status", "role" })
    Account follower;
}
