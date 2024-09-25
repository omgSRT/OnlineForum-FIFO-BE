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
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID pointId;
    double maxPoint;
    double pointPerPost;

    @JsonIgnoreProperties(value = { "point" }, allowSetters = true)
    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DailyPoint> dailyPointList;
}
