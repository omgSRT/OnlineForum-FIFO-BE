package com.FA24SE088.OnlineForum.repository;

import com.FA24SE088.OnlineForum.entity.*;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT DISTINCT c " +
            "FROM Comment c LEFT JOIN FETCH c.replies r " +
            "WHERE c.post = :post AND c.parentComment IS NULL")
    List<Comment> findAllByPostWithReplies(@Param("post") Post post);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Comment>> findByAccount(Account account);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Comment>> findAllByAccountOrderByCreatedDateDesc(Account account);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Integer> countByPostTopicCategory(Category category);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Integer> countByPostTopic(Topic topic);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Integer> countByPost(Post post);

    List<Comment> findAllByOrderByCreatedDateDesc();
}
