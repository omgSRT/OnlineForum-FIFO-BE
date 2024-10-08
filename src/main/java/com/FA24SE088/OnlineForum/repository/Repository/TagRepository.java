package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<Boolean> existsByNameContaining(String name);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Boolean> existsByName(String name);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Tag>> findByNameContaining(String name);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Tag>> findByName(String name);
}
