package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    List<Follow> findByFollower(Account follower);
    Optional<Follow> findByFollowerAndFollowee(Account follower, Account followee);
    boolean existsByFollowerAndAndFollowee(Account follower, Account followee);
    List<Follow> findByFollowee(Account followee);

    @Query("SELECT f.followee, COUNT(f.follower) AS followerCount " +
            "FROM Follow f " +
            "GROUP BY f.followee " +
            "ORDER BY followerCount DESC")
    List<Object[]> findTop10MostFollowedAccounts(Pageable pageable);
    long countByFollowee(Account followee);
    long countByFollower(Account follower);
}
