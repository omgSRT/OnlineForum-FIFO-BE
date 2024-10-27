package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID sectionCodeId;
    Date createdDate;
    String linkGit;
    Integer sectionOrder;
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;

    @ManyToOne
    @JoinColumn(name = "sectionId")
    Document document;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "section" }, allowSetters = true)
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ImageSection> imageSectionList;

    @JsonIgnore
    @JsonIgnoreProperties(value = { "section" }, allowSetters = true)
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VideoSection> videoSectionList;
}
