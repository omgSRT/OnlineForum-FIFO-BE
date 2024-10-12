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
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID notificationId;
    String title;
    String message;
    boolean isRead;
    Date createdDate;
    String type;


    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;
}
