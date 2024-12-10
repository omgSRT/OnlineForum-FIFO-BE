package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.TypeBonus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyPointResponse {
    UUID dailyPointId;
    double pointEarned;
    Date createdDate;
    @JsonIgnoreProperties(value = {"password", "bio", "coverImage", "gender", "address", "createdDate", "status", "role"})
    Account account;
    @JsonIgnoreProperties(value = {"account", "topic", "tag", "status"})
    Post post;
    @JsonIgnoreProperties(value = {"dailyPointList"})
    TypeBonus typeBonus;
}
