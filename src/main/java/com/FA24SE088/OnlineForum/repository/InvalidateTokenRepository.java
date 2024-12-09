package com.FA24SE088.OnlineForum.repository;

import com.FA24SE088.OnlineForum.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidateTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
