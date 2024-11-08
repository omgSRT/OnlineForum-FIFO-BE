package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.OrderPoint;
import com.FA24SE088.OnlineForum.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface OrderPointRepository extends JpaRepository<OrderPoint, UUID> {
    CompletableFuture<List<OrderPoint>> findByWallet_AccountAndOrderDateBetweenOrderByOrderDateDesc(Account account, Date start, Date end);
    CompletableFuture<List<OrderPoint>> findByWallet_AccountOrderByOrderDateDesc(Account account);
}
