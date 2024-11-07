package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.entity.Pricing;
import com.FA24SE088.OnlineForum.entity.Wallet;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingRequest {
    long price;
    double point;
}
