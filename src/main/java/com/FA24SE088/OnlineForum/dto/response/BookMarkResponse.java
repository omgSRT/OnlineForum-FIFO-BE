package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookMarkResponse {
    UUID bookmarkID;
    @JsonIgnoreProperties(value = {"redeemList","notificationList","followeeList","followerList","dailyPointList","postList","upvoteList","commentList","categoryList","eventList"}, allowSetters = true)
    Account account;
    @JsonIgnoreProperties(value = { "account", "topic", "tag" })
    Post post;
}
