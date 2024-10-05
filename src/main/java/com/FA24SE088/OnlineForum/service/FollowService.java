package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Follow;
import com.FA24SE088.OnlineForum.enums.FollowStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springfox.documentation.spi.service.contexts.SecurityContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class FollowService {
    @Autowired
    UnitOfWork unitOfWork;
    @Autowired
    PaginationUtils paginationUtils;

    private Account getCurrentUser(){
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    public Follow create(UUID id) {
        Account account = getCurrentUser();
        Account account1 = unitOfWork.getAccountRepository().findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return Follow.builder()
                .follower(account)
                .followee(account1)
                .status(FollowStatus.FOLLOWING.name())
                .build();
    }

    public List<String> unfollow(UUID id){
        Account account = getCurrentUser();
        Account account1 = unitOfWork.getAccountRepository().findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        account.getFolloweeList().removeIf(follow ->
               follow.getFollowee().getAccountId().equals(account1.getAccountId())
        );
        return account.getFolloweeList().stream()
                .map(follow -> follow.getFollowee().getUsername()) // Giả sử bạn muốn lấy username của followee
                .collect(Collectors.toList());
    }



}
