package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByOrderByCreatedDateDesc();
}
