package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Image;
import com.FA24SE088.OnlineForum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Image>> findByPost(Post post);
}
