package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import com.FA24SE088.OnlineForum.entity.Wallet;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPointRequest {
    double amount;
    String status;
    Date orderDate;
    Wallet wallet;
    MonkeyCoinPack monkeyCoinPack;
}
