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
public class TypeBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID typeBonusId;
    String name;
    String quantity;
    double pointBonus;

    @JsonIgnoreProperties(value = { "point" }, allowSetters = true)
    @OneToMany(mappedBy = "typeBonus", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DailyPoint> dailyPointList;
}
