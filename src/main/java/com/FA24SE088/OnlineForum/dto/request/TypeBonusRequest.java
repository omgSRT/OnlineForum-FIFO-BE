package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.enums.TypeBonusNameEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeBonusRequest {
    @NotNull(message = "Name Cannot Be Null")
    TypeBonusNameEnum name;
    @Min(value = 1L, message = "Quantity Must Be Equal Or Greater Than 1")
    @Max(value = Long.MAX_VALUE, message = "Quantity Must Be Equal Or Lesser Than 9 Quintillion")
    long quantity;
    @Min(value = 1L, message = "Point Bonus Must Be Equal Or Greater Than 1")
    double pointBonus;
}
