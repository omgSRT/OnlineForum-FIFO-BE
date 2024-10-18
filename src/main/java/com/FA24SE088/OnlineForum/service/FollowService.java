package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.FollowResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.BlockedAccount;
import com.FA24SE088.OnlineForum.entity.Follow;
import com.FA24SE088.OnlineForum.enums.FollowStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.AccountMapper;
import com.FA24SE088.OnlineForum.mapper.FollowMapper;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class FollowService {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    UnitOfWork unitOfWork;
    @Autowired
    PaginationUtils paginationUtils;
    @Autowired
    FollowMapper followMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public FollowResponse create(UUID id) {
        Account account = getCurrentUser();
        Account account1 = unitOfWork.getAccountRepository().findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Follow follow = Follow.builder()
                .follower(account)
                .followee(account1)
                .status(FollowStatus.FOLLOWING.name())
                .build();
        unitOfWork.getFollowRepository().save(follow);
        return followMapper.toRespone(follow);
    }

    public void unfollow(UUID id){

    }

    public void blockUser(UUID accountIdToBlock) {
        Account currentUser = getCurrentUser();
        // Tìm tài khoản cần chặn
        Account accountToBlock = unitOfWork.getAccountRepository().findById(accountIdToBlock)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        // Kiểm tra xem đã chặn chưa
        boolean alreadyBlocked = currentUser.getBlockedAccounts().stream()
                .anyMatch(blocked -> blocked.getBlocked().getAccountId().equals(accountToBlock.getAccountId()));

        if (!alreadyBlocked) {
            BlockedAccount blockedAccount = new BlockedAccount();
            blockedAccount.setBlocker(currentUser);
            blockedAccount.setBlocked(accountToBlock);
            blockedAccount.setBlockedDate(new Date());

            unitOfWork.getBlockedAccountRepository().save(blockedAccount);
        }
    }

    public List<FollowResponse> getFollowedAccounts() {
        Account currentUser = getCurrentUser();

        List<Follow> followedAccounts = unitOfWork.getFollowRepository().findByFollower(currentUser);

        return followedAccounts.stream()
                .map(followMapper::toRespone)
                .toList();
    }

    public List<AccountResponse> listBlock() {
        Account currentUser = getCurrentUser();

        List<BlockedAccount> blockedAccounts = unitOfWork.getBlockedAccountRepository()
                .findByBlocker(currentUser);

        return blockedAccounts.stream()
                .map(BlockedAccount::getBlocked)
                .map(accountMapper::toResponse)
                .toList();
    }


}
