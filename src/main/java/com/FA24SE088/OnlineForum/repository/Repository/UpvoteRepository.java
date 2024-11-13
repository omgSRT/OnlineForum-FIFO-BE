package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Upvote>> findByPost(Post post);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Integer> countByPost(Post post);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Upvote>> findByAccount(Account account);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Optional<Upvote>> findByPostAndAccount(Post post, Account account);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Integer> countByPostTopic(Topic topic);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Integer> countByPostTopicCategory(Category category);
}
