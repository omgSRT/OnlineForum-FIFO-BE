package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.enums.TransactionType;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionRequest {
    @Min(value = 0, message = "Amount transaction must be greater than or equal to 0")
    double amount;
    UUID accountId;
    UUID rewardId;
    TransactionType transactionType;
}
