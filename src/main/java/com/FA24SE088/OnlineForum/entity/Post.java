package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchProfile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    UUID postId;
    @EqualsAndHashCode.Include
    String title;
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;
    @EqualsAndHashCode.Include
    Date createdDate;
    Date lastModifiedDate;
    String status;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<Image> imageList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"post"}, allowSetters = true)
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    DailyPoint dailyPoint;

    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Upvote> upvoteList;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> commentList;

    @ManyToOne
    @JoinColumn(name = "topicId")
    Topic topic;

    @ManyToOne
    @JoinColumn(name = "tagId")
    Tag tag;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "post" }, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Report> reportList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"post"}, allowSetters = true)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BookMark> bookMarkList;
}
