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
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID reportId;
    String title;
    String description;
    Date reportTime;
    String status;

    @ManyToOne
    @JoinColumn(name = "accountID")
    Account account;

    @ManyToOne
    @JoinColumn(name = "postID")
    Post post;
}
