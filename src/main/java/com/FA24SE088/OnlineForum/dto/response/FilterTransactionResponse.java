package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilterTransactionResponse {
    @JsonIgnoreProperties(value = { "password", "email", "bio", "gender", "address", "createdDate", "status", "role" })
    List<Account> accountList;
    List<Transaction> transactionList;
    List<DailyPoint> dailyPointList;

}
