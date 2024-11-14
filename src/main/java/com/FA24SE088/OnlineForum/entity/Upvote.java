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
@Table(name = "upvote", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "account_id"})
})
public class Upvote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID upvoteId;

    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;

    @ManyToOne
    @JoinColumn(name = "postId")
    Post post;
}
