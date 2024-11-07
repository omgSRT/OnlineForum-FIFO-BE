package com.FA24SE088.OnlineForum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pricing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID pricingId;
    long price;
    double point;

    @OneToMany(mappedBy = "pricing",cascade = CascadeType.ALL,orphanRemoval = true)
    List<OrderPoint> orderPointList;
}
