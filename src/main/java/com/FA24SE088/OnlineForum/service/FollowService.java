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


    //xem danh sách người mình follow
    public List<AccountForFollowedResponse> getFollows() {
        Account currentUser = getCurrentUser();

        List<Follow> followedAccounts = followRepository.findByFollower(currentUser);

        return followedAccounts.stream()
                .map(Follow::getFollowee)
                .map(account -> {
                    long followerCount = followRepository.countByFollowee(account);
                    long followeeCount = followRepository.countByFollower(account);
                    boolean isFollowing = followRepository.existsByFollowerAndAndFollowee(currentUser,account);

                    AccountForFollowedResponse response = accountMapper.toAccountFollowedResponse(account);
                    response.setCountFollowee(followeeCount);
                    response.setCountFollower(followerCount);
                    response.setFollowing(isFollowing);
                    return response;
                })
                .toList();
    }

    //xem danh sách người follow mình
//    public List<FollowResponse> getFollowers() {
//        Account currentUser = getCurrentUser();
//        List<Follow> followers = followRepository.findByFollowee(currentUser);
//
//        return followers.stream()
//                .map(followMapper::toRespone)
//                .toList();
//    }
    //xem danh sách người follow mình
    public List<AccountForFollowedResponse> getFollowers() {
        Account currentUser = getCurrentUser();
        List<Follow> followers = followRepository.findByFollowee(currentUser);

        return followers.stream()
                .map(Follow::getFollower)
                .map(account -> {
                    long followerCount = followRepository.countByFollowee(account);
                    long followeeCount = followRepository.countByFollower(account);
                    boolean isFollowing = followRepository.existsByFollowerAndAndFollowee(currentUser,account);

                    AccountForFollowedResponse response = accountMapper.toAccountFollowedResponse(account);
                    response.setCountFollowee(followeeCount);
                    response.setCountFollower(followerCount);
                    response.setFollowing(isFollowing);
                    return response;
                })
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
