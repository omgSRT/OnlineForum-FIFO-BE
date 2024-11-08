package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.entity.Pricing;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
    Pricing pricing;
//    @JsonIgnoreProperties(value = {"account","transactionList","orderPointList"})
//    Wallet wallet;

}
