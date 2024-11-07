package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.entity.Pricing;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.enums.RoleAccount;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

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
    Pricing pricing;
}
