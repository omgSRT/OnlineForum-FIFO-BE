package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<Wallet> findByAccountAccountId(UUID accountId);

}
