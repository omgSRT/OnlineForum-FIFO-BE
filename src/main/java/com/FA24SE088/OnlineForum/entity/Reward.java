package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID rewardId;
    String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    String image;
    double price;
    String status;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    @Column(columnDefinition = "MEDIUMTEXT")
    String linkSourceCode;
    Date createdDate;

    @JsonIgnoreProperties(value = {"reward", "account"}, allowSetters = true)
    @JsonIgnore
    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Redeem> redeemList;

    @JsonIgnoreProperties(value = { "reward"}, allowSetters = true)

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Transaction> transactionList;
}
