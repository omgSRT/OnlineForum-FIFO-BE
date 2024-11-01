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
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID mediaId;
    @Column(columnDefinition = "MEDIUMTEXT")
    String link;
    Integer number;

    @ManyToOne
    @JoinColumn(name = "contentSectionId")
    ContentSection contentSection;
}
