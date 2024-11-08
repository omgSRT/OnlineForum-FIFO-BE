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
    @JsonIgnoreProperties(value = {"wallet"})
    List<TransactionResponse> transactionList;
    @JsonIgnoreProperties(value = {"account","typeBonus"})
    List<DailyPoint2Response> dailyPointList;
    @JsonIgnoreProperties(value = {"account","point"})
    List<DailyPoint2Response> bonusPoint;
    List<OrderPointResponse> orderPointList;
}
