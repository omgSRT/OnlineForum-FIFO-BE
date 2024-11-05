package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUsername (String username);
    Account findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Account> findByUsernameContaining(String username);

    @Async("AsyncTaskExecutor")
    CompletableFuture<Optional<Account>> findByEmailIgnoreCase(String email);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Account>> findByUsernameContainingIgnoreCase(String username);

    @Async("AsyncTaskExecutor")
    CompletableFuture<List<Account>> findByEmailContainingIgnoreCase(String email);
    List<Account> findAllByStatusAndBannedUntilBefore(String status, LocalDateTime dateTime);

//    List<Account> findAllByStatusAndBannedUntilBefore(AccountStatus status, LocalDateTime dateTime);
}
