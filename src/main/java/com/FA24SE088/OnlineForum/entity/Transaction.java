package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID transactionId;
    double amount;
    Date createdDate;
    String transactionType;

    @ManyToOne
    @JoinColumn(name = "walletId")
    Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "rewardId")
    @JsonIgnoreProperties(value = {"transactionList"}, allowSetters = true)
    Reward reward;
}
