package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisHash;

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

    @JsonIgnore
    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Notification> notificationList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    Wallet wallet;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "followee" }, allowSetters = true)
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Follow> followeeList;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "follower" }, allowSetters = true)
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Follow> followerList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Redeem> redeemList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DailyPoint> dailyPointList;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Post> postList;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Upvote> upvoteList;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> commentList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Category> categoryList;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"account"}, allowSetters = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Event> eventList;
}
