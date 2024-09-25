package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID postId;
    String title;
    String content;
    Date createdDate;
    Date lastModifiedDate;
    String status;

    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Image> imageList;

    @JsonIgnoreProperties(value = {"post"}, allowSetters = true)
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    DailyPoint dailyPoint;

    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;

    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Upvote> upvoteList;

    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> commentList;

    @ManyToOne
    @JoinColumn(name = "topicId")
    Topic topic;

    @ManyToOne
    @JoinColumn(name = "tagId")
    Tag tag;

    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Feedback> feedbackList;
}
