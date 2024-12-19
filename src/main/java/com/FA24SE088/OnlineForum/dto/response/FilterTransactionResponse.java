package com.FA24SE088.OnlineForum.dto.response;

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
    @JsonIgnoreProperties(value = {"wallet"})
    List<TransactionResponse> transactionList;
    @JsonIgnoreProperties(value = {"account", "typeBonus"})
    List<DailyPointForFilterTransactionResponse> dailyPointList;
    @JsonIgnoreProperties(value = {"account", "point"})
    List<DailyPointForFilterTransactionResponse> bonusPoint;
    List<OrderPointResponse> orderPointList;
}
