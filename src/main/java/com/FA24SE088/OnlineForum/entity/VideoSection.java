package com.FA24SE088.OnlineForum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID videoSectionId;
    String url;

    @ManyToOne
    @JoinColumn(name = "sectionId")
    Section section;
}
