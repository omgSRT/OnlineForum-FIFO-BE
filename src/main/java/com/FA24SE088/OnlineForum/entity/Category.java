package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID categoryId;
    String name;
    String image;

    @JsonIgnoreProperties(value = {"category"}, allowSetters = true)
    @OneToOne
    @JoinColumn(name = "accountId")
    Account account;

    @JsonIgnoreProperties(value = { "category" }, allowSetters = true)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Topic> topicList;
}
