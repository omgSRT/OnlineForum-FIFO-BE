package com.FA24SE088.OnlineForum.repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Redeem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RedeemRepository extends JpaRepository<Redeem, UUID> {
    List<Redeem> findByAccount_AccountId(UUID accountId);

    List<Redeem> findAllByAccount(Account account);

}
