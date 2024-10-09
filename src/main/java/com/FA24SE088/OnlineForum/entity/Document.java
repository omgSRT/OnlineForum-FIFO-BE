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
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID documentId;
    String name;
    String image;
    double price;
    String type;
    String status;

    @JsonIgnoreProperties(value = {"reward"}, allowSetters = true)
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Redeem> redeemList;

    @JsonIgnoreProperties(value = { "document" }, allowSetters = true)
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Section> sectionList;
}
