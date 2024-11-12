package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.UnfollowRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.BlockedAccount;
import com.FA24SE088.OnlineForum.entity.Follow;
import com.FA24SE088.OnlineForum.enums.FollowStatus;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class BlockUserService {
    AccountMapper accountMapper;
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    FollowMapper followMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public BlockAccountResponse blockOrUnblock(UUID accountIdToToggle) {
        Account currentUser = getCurrentUser();
        Account accountToToggle = unitOfWork.getAccountRepository().findById(accountIdToToggle)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (currentUser.getAccountId().equals(accountToToggle.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_BLOCK_SELF);
        }

        Optional<BlockedAccount> blockedAccountOptional = unitOfWork.getBlockedAccountRepository()
                .findByBlockerAndBlocked(currentUser, accountToToggle);

        BlockAccountResponse response = new BlockAccountResponse();
        response.setFollowId(accountIdToToggle);

        if (blockedAccountOptional.isPresent()) {
            unitOfWork.getBlockedAccountRepository().delete(blockedAccountOptional.get());
            response.setMessage(SuccessReturnMessage.DELETE_SUCCESS.getMessage());
        } else {
            BlockedAccount blockedAccount = new BlockedAccount();
            blockedAccount.setBlocker(currentUser);
            blockedAccount.setBlocked(accountToToggle);
            blockedAccount.setBlockedDate(new Date());

            unitOfWork.getBlockedAccountRepository().save(blockedAccount);
            response.setMessage(SuccessReturnMessage.CREATE_SUCCESS.getMessage());
        }

        response.setBlocker(currentUser);
        response.setBlocked(accountToToggle);

        return response;
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
