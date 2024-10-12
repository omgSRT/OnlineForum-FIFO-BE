package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Comment;
import com.FA24SE088.OnlineForum.entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    CompletableFuture<List<Comment>> findByAccount(Account account);
}
