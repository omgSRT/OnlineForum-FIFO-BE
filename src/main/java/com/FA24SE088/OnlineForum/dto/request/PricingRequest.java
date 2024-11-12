package com.FA24SE088.OnlineForum.dto.request;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingRequest {
    @Column(columnDefinition = "MEDIUMTEXT")
    String imgUrl;
    long price;
    double point;
}
