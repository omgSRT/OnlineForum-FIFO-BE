package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    UUID walletId;
    @EqualsAndHashCode.Include
    double balance;

    @JsonIgnoreProperties(value = {"wallet"}, allowSetters = true)
    @OneToOne
    @JoinColumn(name = "accountId")
    @EqualsAndHashCode.Include
    Account account;

    @JsonIgnoreProperties(value = {"wallet"}, allowSetters = true)
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Transaction> transactionList;

    @JsonIgnoreProperties(value = {"wallet"}, allowSetters = true)
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderPoint> orderPointList;
}
