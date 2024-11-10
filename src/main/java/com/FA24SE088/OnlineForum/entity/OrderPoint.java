package com.FA24SE088.OnlineForum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID orderId;
    double amount;
    String status;
    Date orderDate;

    @ManyToOne
    @JoinColumn(name = "monkeyCoinPackId")
    MonkeyCoinPack monkeyCoinPack;

    @ManyToOne
    @JoinColumn(name = "walletId")
    Wallet wallet;
}
