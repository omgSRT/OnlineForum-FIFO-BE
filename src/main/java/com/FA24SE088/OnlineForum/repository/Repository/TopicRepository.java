package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<Boolean> existsByNameContaining(String name);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Topic>> findByCategoryCategoryId(UUID categoryId);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Topic> findByName(String name);
}
