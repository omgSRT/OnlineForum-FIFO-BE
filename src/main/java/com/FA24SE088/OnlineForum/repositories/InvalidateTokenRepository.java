package com.FA24SE088.OnlineForum.repositories;

import com.FA24SE088.OnlineForum.entities.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidateTokenRepository extends JpaRepository<InvalidatedToken,String> {
}
