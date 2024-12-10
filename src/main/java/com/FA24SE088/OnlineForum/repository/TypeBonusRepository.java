package com.FA24SE088.OnlineForum.repository;

import com.FA24SE088.OnlineForum.entity.TypeBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface TypeBonusRepository extends JpaRepository<TypeBonus, UUID> {
    @Async("AsyncTaskExecutor")
    CompletableFuture<TypeBonus> findByNameAndQuantity(String name, long quantity);
}
