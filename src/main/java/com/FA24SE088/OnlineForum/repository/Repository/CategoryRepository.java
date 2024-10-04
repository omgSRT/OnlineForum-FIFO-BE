package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<Boolean> existsByNameContaining(String name);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Category>> findByAccountAccountId(UUID accountId);

    Optional<Category> findByName(String name);
}
