package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Upvote>> findByPost(Post post);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Upvote> findByPostAndAccount(Post post, Account account);
}
