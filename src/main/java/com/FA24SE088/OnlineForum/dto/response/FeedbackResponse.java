package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {
    UUID feedbackId;
    String title;
    String content;
    String status;
    LocalDateTime createdDate;
    @JsonIgnoreProperties(value = {"password", "followerList", "redeemList", "dailyPointList", "postList", "upvoteList", "commentList", "categoryList", "eventList", "blockedAccounts", "feedbackList", "reportList", "reportsReceived", "bookMarkList", "followeeList", "address", "createdDate", "status", "role"})
    Account account;
//     @JsonIgnoreProperties(value = { "account"})
//     Notification notification;
}


