package com.FA24SE088.OnlineForum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID followId;
    String status;

    @ManyToOne
    @JoinColumn(name = "followeeId")
    Account followee;

    @ManyToOne
    @JoinColumn(name = "followerId")
    Account follower;
}
