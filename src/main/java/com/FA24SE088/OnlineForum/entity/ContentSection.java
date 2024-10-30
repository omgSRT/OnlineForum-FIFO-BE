package com.FA24SE088.OnlineForum.entity;

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
public class ContentSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID contentSectionId;
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;
    @Column(columnDefinition = "MEDIUMTEXT")
    String code;
    Integer number;

    @ManyToOne
    @JoinColumn(name = "sectionCodeId")
    Section section;

    @OneToMany(mappedBy = "contentSection", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Media> medias;
}
