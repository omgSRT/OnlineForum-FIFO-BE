package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID accountId;
    String username;
    String handle;
    String email;
    String password;
    String bio;
    String gender;
    String address;
    String avatar;
    Date createdDate;
    String status;


    @ManyToOne
    @JoinColumn(name = "roleId")
    Role role;

    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Notification> notificationList;

    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    Wallet wallet;

    @JsonIgnoreProperties(value = { "followee" }, allowSetters = true)
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Follow> followeeList;

    @JsonIgnoreProperties(value = { "follower" }, allowSetters = true)
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Follow> followerList;

    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Redeem> redeemList;

    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DailyPoint> dailyPointList;

    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Post> postList;

    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Upvote> upvoteList;

    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> commentList;

    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Category> categoryList;

    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Event> eventList;
}
