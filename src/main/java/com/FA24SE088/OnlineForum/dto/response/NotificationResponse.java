package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
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
public class NotificationResponse {
    UUID notificationId;
    String title;
    String message;
    boolean isRead;
    Date createdDate;
    @JsonIgnoreProperties(value = {"password", "followerList", "redeemList", "dailyPointList", "postList", "upvoteList", "commentList", "categoryList", "eventList", "blockedAccounts", "feedbackList", "reportList", "reportsReceived", "bookMarkList", "followeeList", "address", "createdDate", "status"})
    Account account;
}
