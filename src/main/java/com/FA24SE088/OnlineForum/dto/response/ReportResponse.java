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
public class ReportResponse {
    UUID reportId;
    String title;
    String description;
    Date reportTime;
    String status;
    UUID postId;
    String postTitle;
    String postContent;
    Date postCreatedDate;
    Date postLastModifiedDate;
    String postStatus;
    @JsonIgnoreProperties(value = { "password", "email", "bio", "gender", "address", "createdDate", "status", "role" })
    Account account;
}
