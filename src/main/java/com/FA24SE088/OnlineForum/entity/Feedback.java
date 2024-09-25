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
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID feedbackId;
    String title;
    String content;
    String status;

    @ManyToOne
    @JoinColumn(name = "postId")
    Post post;
}