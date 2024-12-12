package com.FA24SE088.OnlineForum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID feedbackId;
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;
    String title;
    String status;

    @CreationTimestamp
    LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "accountID")
    Account account;

}



