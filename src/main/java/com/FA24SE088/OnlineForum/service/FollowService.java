package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.UnfollowRequest;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class FollowService {
    AccountMapper accountMapper;
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    FollowMapper followMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public FollowResponse create(UUID id) {
        Account account = getCurrentUser();
        Account account1 = unitOfWork.getAccountRepository().findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (account.getAccountId().equals(account1.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // Kiểm tra nếu currentUser đã theo dõi account1 hay chưa
        boolean alreadyFollow = account.getFollowerList().stream()
                .anyMatch(follow -> follow.getFollowee().getAccountId().equals(account1.getAccountId()));

        if(alreadyFollow){
            throw new AppException(ErrorCode.ACCOUNT_HAS_BEEN_FOLLOWED);
        }
        else {
            Follow follow = Follow.builder()
                    .follower(account)
                    .followee(account1)
                    .status(FollowStatus.FOLLOWING.name())
                    .build();
            return followMapper.toRespone(unitOfWork.getFollowRepository().save(follow));
        }
    }


    public void unfollow(UnfollowRequest request){
        Account account = getCurrentUser();
        Account follower = unitOfWork.getAccountRepository().findById(request.getAccountID())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (account.getAccountId().equals(follower.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_UNFOLLOW_SELF);
        }

        Follow follow = unitOfWork.getFollowRepository().findByFollowerAndFollowee(account, follower)
                .orElseThrow(() -> new AppException(ErrorCode.FOLLOW_NOT_FOUND));

        unitOfWork.getFollowRepository().delete(follow);
    }

    public void blockUser(UUID accountIdToBlock) {
        Account currentUser = getCurrentUser();
        Account accountToBlock = unitOfWork.getAccountRepository().findById(accountIdToBlock)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (currentUser.getAccountId().equals(accountToBlock.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_BLOCK_SELF);
        }
        // Kiểm tra nếu currentUser đã follow accountToBlock
        Optional<Follow> followOptional = unitOfWork.getFollowRepository().findByFollowerAndFollowee(currentUser, accountToBlock);

        followOptional.ifPresent(follow -> unitOfWork.getFollowRepository().delete(follow));

        boolean alreadyBlocked = currentUser.getBlockedAccounts().stream()
                .anyMatch(blocked -> blocked.getBlocked().getAccountId().equals(accountToBlock.getAccountId()));

        if (!alreadyBlocked) {
            BlockedAccount blockedAccount = new BlockedAccount();
            blockedAccount.setBlocker(currentUser);
            blockedAccount.setBlocked(accountToBlock);
            blockedAccount.setBlockedDate(new Date());

            unitOfWork.getBlockedAccountRepository().save(blockedAccount);
        }
        else{
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_BLOCKED);
        }
    }

    public void unblock(UUID accountIdToUnblock) {
        Account currentUser = getCurrentUser();
        Account accountToUnblock = unitOfWork.getAccountRepository().findById(accountIdToUnblock)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (currentUser.getAccountId().equals(accountToUnblock.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_UNBLOCK_SELF);
        }
        BlockedAccount blockedAccount = unitOfWork.getBlockedAccountRepository()
                .findByBlockerAndBlocked(currentUser, accountToUnblock)
                .orElseThrow(() -> new AppException(ErrorCode.BLOCK_NOT_FOUND));

        unitOfWork.getBlockedAccountRepository().delete(blockedAccount);
    }

//xem danh sách người mình follow
    public List<FollowResponse> getFollows() {
        Account currentUser = getCurrentUser();

        List<Follow> followedAccounts = unitOfWork.getFollowRepository().findByFollower(currentUser);

        return followedAccounts.stream()
                .map(followMapper::toRespone)
                .toList();
    }
//xem danh sách người follow mình
    public List<FollowResponse> getFollowers() {
        Account currentUser = getCurrentUser();

        // Lấy danh sách các đối tượng follow mà followee là người dùng hiện tại
        List<Follow> followers = unitOfWork.getFollowRepository().findByFollowee(currentUser);

        return followers.stream()
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
