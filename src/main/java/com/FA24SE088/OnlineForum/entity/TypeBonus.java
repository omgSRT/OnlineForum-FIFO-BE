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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID typeBonusId;
    String name;
    long quantity;
    double pointBonus;

    @JsonIgnore
    @JsonIgnoreProperties(value = {"point", "typeBonus"}, allowSetters = true)
    @OneToMany(mappedBy = "typeBonus", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DailyPoint> dailyPointList;
}
