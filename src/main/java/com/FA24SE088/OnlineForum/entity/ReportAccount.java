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
public class ReportAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID reportAccountId;
    String reason;
    Date reportTime;
    String status;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private Account reporter;

    @ManyToOne
    @JoinColumn(name = "reported_id")
    private Account reported;

}
