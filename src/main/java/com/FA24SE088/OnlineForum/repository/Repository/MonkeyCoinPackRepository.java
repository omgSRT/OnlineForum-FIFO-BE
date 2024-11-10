package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonkeyCoinPackRepository extends JpaRepository<MonkeyCoinPack, UUID> {
    Optional<MonkeyCoinPack> findByPoint(double point);
}
