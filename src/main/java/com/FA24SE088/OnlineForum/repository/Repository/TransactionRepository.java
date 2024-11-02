package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Reward;
import com.FA24SE088.OnlineForum.entity.Transaction;
import com.FA24SE088.OnlineForum.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Transaction>> findAllByOrderByCreatedDateDesc();
    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Transaction>> findAllByOrderByCreatedDateAsc();
    @Async("AsyncTaskExecutor")
    CompletableFuture<Boolean> existsByAccountAndReward(Account account, Reward reward);
    List<Transaction> findByWallet(Wallet wallet);
}
