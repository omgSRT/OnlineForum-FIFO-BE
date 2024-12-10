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
public class BlockedAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID blockId;

    @ManyToOne
    @JoinColumn(name = "blocker_id")
    Account blocker;

    // Tài khoản bị chặn
    @ManyToOne
    @JoinColumn(name = "blocked_account_id")
    Account blocked;

    Date blockedDate;
}
