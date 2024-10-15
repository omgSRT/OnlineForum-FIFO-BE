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
public class ImageFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID imageFeedbackId;
    String url;

//    @ManyToOne
//    @JoinColumn(name = "feedbackID")
//    Feedback feedback;

}
