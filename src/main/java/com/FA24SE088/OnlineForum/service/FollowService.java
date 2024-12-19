package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.FollowStatus;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.enums.WebsocketEventName;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.AccountMapper;
import com.FA24SE088.OnlineForum.mapper.FollowMapper;
import com.FA24SE088.OnlineForum.repository.NotificationRepository;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import com.FA24SE088.OnlineForum.utils.SocketIOUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.BlockedAccountRepository;
import com.FA24SE088.OnlineForum.repository.FollowRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class FollowService {
    AccountMapper accountMapper;
    PaginationUtils paginationUtils;
    ObjectMapper objectMapper = new ObjectMapper();
    SocketIOUtil socketIOUtil;
    AccountRepository accountRepository;
    FollowRepository followRepository;
    NotificationRepository notificationRepository;
    BlockedAccountRepository blockedAccountRepository;
    FollowMapper followMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

//    public FollowResponse create(UUID id) {
//        Account account = getCurrentUser();
//        Account account1 = accountRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        if (account.getAccountId().equals(account1.getAccountId())) {
//            throw new AppException(ErrorCode.CANNOT_FOLLOW_SELF);
//        }
//        boolean alreadyFollow = account.getFollowerList().stream()
//                .anyMatch(follow -> follow.getFollowee().getAccountId().equals(account1.getAccountId()));
//
//        if (alreadyFollow) {
//            throw new AppException(ErrorCode.ACCOUNT_HAS_BEEN_FOLLOWED);
//        } else {
//            Follow follow = Follow.builder()
//                    .follower(account)
//                    .followee(account1)
//                    .status(FollowStatus.FOLLOWING.name())
//                    .build();
//            return followMapper.toRespone(followRepository.save(follow));
//        }
//    }

//    public void unfollow(UnfollowRequest request) {
//        Account account = getCurrentUser();
//        Account follower = accountRepository.findById(request.getAccountID())
//                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        if (account.getAccountId().equals(follower.getAccountId())) {
//            throw new AppException(ErrorCode.CANNOT_UNFOLLOW_SELF);
//        }
//
//        Follow follow = followRepository.findByFollowerAndFollowee(account, follower)
//                .orElseThrow(() -> new AppException(ErrorCode.FOLLOW_NOT_FOUND));
//
//        followRepository.delete(follow);
//    }
//
//    public void blockUser(UUID accountIdToBlock) {
//        Account currentUser = getCurrentUser();
//        Account accountToBlock = accountRepository.findById(accountIdToBlock)
//                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        if (currentUser.getAccountId().equals(accountToBlock.getAccountId())) {
//            throw new AppException(ErrorCode.CANNOT_BLOCK_SELF);
//        }
//        // Kiểm tra nếu currentUser đã follow accountToBlock
//        Optional<Follow> followOptional = followRepository.findByFollowerAndFollowee(currentUser, accountToBlock);
//
//        followOptional.ifPresent(followRepository::delete);
//
//        boolean alreadyBlocked = currentUser.getBlockedAccounts().stream()
//                .anyMatch(blocked -> blocked.getBlocked().getAccountId().equals(accountToBlock.getAccountId()));
//
//        if (!alreadyBlocked) {
//            BlockedAccount blockedAccount = new BlockedAccount();
//            blockedAccount.setBlocker(currentUser);
//            blockedAccount.setBlocked(accountToBlock);
//            blockedAccount.setBlockedDate(new Date());
//
//            blockedAccountRepository.save(blockedAccount);
//        } else {
//            throw new AppException(ErrorCode.ACCOUNT_ALREADY_BLOCKED);
//        }
//    }
//
//    public void unblock(UUID accountIdToUnblock) {
//        Account currentUser = getCurrentUser();
//        Account accountToUnblock = accountRepository.findById(accountIdToUnblock)
//                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        if (currentUser.getAccountId().equals(accountToUnblock.getAccountId())) {
//            throw new AppException(ErrorCode.CANNOT_UNBLOCK_SELF);
//        }
//        BlockedAccount blockedAccount = blockedAccountRepository
//                .findByBlockerAndBlocked(currentUser, accountToUnblock)
//                .orElseThrow(() -> new AppException(ErrorCode.BLOCK_NOT_FOUND));
//
//        blockedAccountRepository.delete(blockedAccount);
//    }

    //xem danh sách người mình follow
    public List<FollowResponse> getFollows() {
        Account currentUser = getCurrentUser();

        List<Follow> followedAccounts = followRepository.findByFollower(currentUser);

        return followedAccounts.stream()
                .map(followMapper::toRespone)
                .toList();
    }

    //xem danh sách người follow mình
    public List<FollowResponse> getFollowers() {
        Account currentUser = getCurrentUser();
        List<Follow> followers = followRepository.findByFollowee(currentUser);

        return followers.stream()
                .map(followMapper::toRespone)
                .toList();
    }

    public FollowOrUnfollowResponse followOrUnfollow(UUID followeeId) {
        Account account = getCurrentUser();

        Account followee = accountRepository.findById(followeeId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getAccountId().equals(followee.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        boolean exist = followRepository.existsByFollowerAndAndFollowee(account, followee);

        FollowOrUnfollowResponse response = new FollowOrUnfollowResponse();
        if (exist) {
            Follow existingFollow = followRepository.findByFollowerAndFollowee(account, followee).orElseThrow(() -> new AppException(ErrorCode.FOLLOW_NOT_FOUND));
            followRepository.delete(existingFollow);
            response.setMessage(SuccessReturnMessage.DELETE_SUCCESS.getMessage());
            response.setFollowee(followee);
            response.setFollower(account);
        } else {
            Follow follow = Follow.builder()
                    .follower(account)
                    .followee(followee)
                    .status(FollowStatus.FOLLOWING.name())
                    .build();
            followRepository.save(follow);
            response = followMapper.toResponse2(follow);

            followRepository.save(follow);

            response.setMessage(SuccessReturnMessage.CREATE_SUCCESS.getMessage());
            response.setFollowee(followee);
            response.setFollower(account);
            realtime_follow(follow, "Follow", "Follow notification",account.getUsername() + " is following you");
        }
        return response;
    }

    public void realtime_follow(Follow follow, String entity, String titleNotification,String message) {
        DataNotification dataNotification = DataNotification.builder()
                .id(follow.getFollowId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(titleNotification)
                    .message(messageJson)
                    .isRead(false)
                    .account(follow.getFollowee())
                    .createdDate(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
            socketIOUtil.sendEventToOneClientInAServer(follow.getFollowee().getAccountId(), WebsocketEventName.NOTIFICATION.name(), message,notification);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public List<AccountResponse> listBlock() {
        Account currentUser = getCurrentUser();

        List<BlockedAccount> blockedAccounts = blockedAccountRepository
                .findByBlocker(currentUser);

        return blockedAccounts.stream()
                .map(BlockedAccount::getBlocked)
                .map(accountMapper::toResponse)
                .toList();
    }

    public List<AccountFollowResponse> getTop10MostFollowedAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = followRepository.findTop10MostFollowedAccounts(pageable);
        List<AccountFollowResponse> top10Accounts = new ArrayList<>();

        for (Object[] result : results) {
            Account account = (Account) result[0];
            long followerCount = (long) result[1];

            AccountFollowResponse accountResponse = accountMapper.toCountFollower(account);
            accountResponse.setCountFollowers(followerCount);

            top10Accounts.add(accountResponse);
        }

        return top10Accounts;
    }

}
