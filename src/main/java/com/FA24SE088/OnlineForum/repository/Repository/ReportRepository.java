package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Report>> findByAccountUsernameContaining(String username);
}
