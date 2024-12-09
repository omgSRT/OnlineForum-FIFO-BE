package com.FA24SE088.OnlineForum.repository;

import com.FA24SE088.OnlineForum.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoSectionRepository extends JpaRepository<Media, UUID> {
}
