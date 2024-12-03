package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.BlockedAccount;
import com.google.cloud.vision.v1.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockedAccountRepository extends JpaRepository<BlockedAccount, UUID> {
    List<BlockedAccount> findByBlocker(Account blocker);
    List<BlockedAccount> findByBlocked(Account blocked);
    Optional<BlockedAccount> findByBlockerAndBlocked(Account blocker, Account blocked);
}
