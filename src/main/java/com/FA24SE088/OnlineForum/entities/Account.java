package com.FA24SE088.OnlineForum.entities;

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
    String password;
    String bio;
    String gender;
    String address;
    String image;
    Date createdDate;
    String status;
    @ManyToOne
    @JoinColumn(name = "roleId")
    Role role;


}
