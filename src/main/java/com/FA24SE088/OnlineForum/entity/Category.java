package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    UUID categoryId;
    @EqualsAndHashCode.Include
    String name;
    @Column(columnDefinition = "LONGTEXT")
    String description;
    @Column(columnDefinition = "MEDIUMTEXT")
    String image;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "accountId")
    Account account;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"category"}, allowSetters = true)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Topic> topicList;
}
