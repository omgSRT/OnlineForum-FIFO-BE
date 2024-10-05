package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.DailyPoint;
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
    CompletableFuture<List<DailyPoint>> findByAccount(Account account);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<DailyPoint>> findByCreatedDate(Date givenDate);
}
