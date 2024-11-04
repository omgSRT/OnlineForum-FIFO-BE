package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeBonusUpdateRequest {
    @Min(value = 1L, message = "Quantity Must Be Equal Or Greater Than 1")
    @Max(value = Long.MAX_VALUE, message = "Quantity Must Be Equal Or Lesser Than 9 Quintillion")
    long quantity;
    @Min(value = 1L, message = "Point Bonus Must Be Equal Or Greater Than 1")
    double pointBonus;
}
