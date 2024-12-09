package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportAccount2Response {
    UUID reportAccountId;
    String title;
    String reason;
    Date reportTime;
    String status;
    @JsonIgnoreProperties(value = {"password", "followerList", "redeemList", "dailyPointList", "postList", "upvoteList", "commentList", "categoryList", "eventList", "blockedAccounts", "feedbackList", "reportList", "reportsReceived", "bookMarkList", "followeeList", "address", "createdDate", "status", "role"})
    Account reporter;//người đi report
    @JsonIgnoreProperties(value = {"password", "followerList", "redeemList", "dailyPointList", "postList", "upvoteList", "commentList", "categoryList", "eventList", "blockedAccounts", "feedbackList", "reportList", "reportsReceived", "bookMarkList", "followeeList", "address", "createdDate", "status", "role"})
    Account reported;//người bị report
    @JsonIgnoreProperties(value = {"reportList", "dailyPoint", "account", "upvoteList", "commentList", "reportList", "bookMarkList", "tag", "topic"})
    List<Post> postOfReportedList;
}


