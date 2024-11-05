package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.DailyPoint;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.TypeBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface DailyPointRepository extends JpaRepository<DailyPoint, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByAccountAndCreatedDate(Account account, Date createdDate);

    @Async("AsyncTaskExecutor")
    CompletableFuture<DailyPoint> findByAccountAndPost(Account account, Post post);

    @Async("AsyncTaskExecutor")
    CompletableFuture<DailyPoint> findByAccountAndPostAndTypeBonus(Account account, Post post, TypeBonus typeBonus);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByAccount(Account account);

    List<DailyPoint> findAllByOrderByCreatedDateDesc();
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByAccountAndTypeBonusIsNullOrderByCreatedDateDesc(Account account);
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByAccountAndTypeBonusIsNotNullOrderByCreatedDateDesc(Account account);
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByAccountAndTypeBonusIsNullAndCreatedDateBetweenOrderByCreatedDateDesc(Account account, Date startDate, Date endDate);
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByAccountAndTypeBonusIsNotNullAndCreatedDateBetweenOrderByCreatedDateDesc(Account account, Date startDate, Date endDate);
}
