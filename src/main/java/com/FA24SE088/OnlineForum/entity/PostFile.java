package com.FA24SE088.OnlineForum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class PostFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID postFileId;
    @Column(columnDefinition = "MEDIUMTEXT")
    String url;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "postId")
    Post post;
}
