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
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID eventId;
    String title;
    Date startDate;
    Date endDate;
    String location;
    String image;
    String content;
    String link;
    String status;

    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;
}
