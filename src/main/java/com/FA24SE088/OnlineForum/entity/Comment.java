package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID commentId;
    String content;
    double rate;

    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "postId")
    Post post;
}
