package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Tag;
import com.FA24SE088.OnlineForum.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Post>> findByStatus(String status);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Post>> findByAccount(Account account);

    List<Post> findAllByOrderByCreatedDateDesc();
}
