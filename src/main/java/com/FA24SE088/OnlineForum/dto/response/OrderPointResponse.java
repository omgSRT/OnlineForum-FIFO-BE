package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
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
public class OrderPointResponse {
    UUID orderId;
    double amount;
    String status;
    Date orderDate;
    @JsonIgnoreProperties(value = {"orderPointList"})
    MonkeyCoinPack monkeyCoinPack;
//    @JsonIgnoreProperties(value = {"account","transactionList","orderPointList"})
//    Wallet wallet;

}
