package com.FA24SE088.OnlineForum.repository;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByAccount(Account account);

    List<Notification> findByAccountOrderByCreatedDateDesc(Account account);

    List<Notification> findAllByOrderByCreatedDateDesc();
}
