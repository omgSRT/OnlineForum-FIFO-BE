package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Role;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    String username;
    String handle;
    String email;
    String bio;
    String gender;
    String address;
    String avatar;
    Date createdDate;
    String status;
    Role role;
    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    List<Category> categoryList;
    @JsonIgnoreProperties(value = {"account","transactionList"}, allowSetters = true)
    Wallet wallet;
}
