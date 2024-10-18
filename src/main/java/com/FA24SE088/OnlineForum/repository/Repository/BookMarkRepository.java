package com.FA24SE088.OnlineForum.repository.Repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, UUID> {
    Optional<BookMark> findByAccountAndPost_PostId(Account account, UUID postId);

    List<BookMark> findByAccount(Account account);
}
