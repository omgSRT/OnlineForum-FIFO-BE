package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.UpvoteRequest;
import com.FA24SE088.OnlineForum.dto.response.DataNotification;
import com.FA24SE088.OnlineForum.dto.response.UpvoteCreateDeleteResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteGetAllResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.enums.TypeBonusNameEnum;
import com.FA24SE088.OnlineForum.enums.WebsocketEventName;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.UpvoteMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import com.FA24SE088.OnlineForum.utils.SocketIOUtil;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UpvoteService {
    UnitOfWork unitOfWork;
    UpvoteMapper upvoteMapper;
    SocketIOUtil socketIOUtil;
    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<UpvoteCreateDeleteResponse> addOrDeleteUpvote(UpvoteRequest request) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();
            var existUpvoteFuture = unitOfWork.getUpvoteRepository().findByPostAndAccount(post, account);

            return existUpvoteFuture.thenCompose(existUpvote -> {
                if (existUpvote.isPresent()) {
                    unitOfWork.getUpvoteRepository().delete(existUpvote.get());
                    var upvoteResponse = upvoteMapper.toUpvoteCreateDeleteResponse(existUpvote.get());
                    upvoteResponse.setMessage(SuccessReturnMessage.DELETE_SUCCESS.getMessage());
                    return CompletableFuture.completedFuture(upvoteResponse);
                } else {
                    return unitOfWork.getUpvoteRepository().countByPost(post)
                            .thenCompose(upvoteAmount -> {
                                long amount = upvoteAmount + 1;
                                return unitOfWork.getTypeBonusRepository().findByNameAndQuantity(TypeBonusNameEnum.UPVOTE.name(), amount)
                                        .thenCompose(typeBonus -> {
                                            if (typeBonus != null) {
                                                return createDailyPointLog(post.getAccount(), post, typeBonus)
                                                        .thenCompose(existingDailyPoint -> {
                                                            Upvote newUpvote = new Upvote();
                                                            newUpvote.setAccount(account);
                                                            newUpvote.setPost(post);
                                                            realtime_upvote(newUpvote,account,"Upvote","Upvote notification");
//                                                            realtime_dailyPoint(existingDailyPoint,post.getAccount(),"Daily Point","Daily Point notification",clientSessionId);
                                                            var upvoteResponse = upvoteMapper.toUpvoteCreateDeleteResponse(unitOfWork.getUpvoteRepository().save(newUpvote));
                                                            upvoteResponse.setMessage(SuccessReturnMessage.CREATE_SUCCESS.getMessage());
                                                            return CompletableFuture.completedFuture(upvoteResponse);
                                                        });
                                            } else {
                                                Upvote newUpvote = new Upvote();
                                                newUpvote.setAccount(account);
                                                newUpvote.setPost(post);
                                                realtime_upvote(newUpvote,account,"Upvote","Upvote notification");
                                                var upvoteResponse = upvoteMapper.toUpvoteCreateDeleteResponse(unitOfWork.getUpvoteRepository().save(newUpvote));
                                                upvoteResponse.setMessage(SuccessReturnMessage.CREATE_SUCCESS.getMessage());
                                                return CompletableFuture.completedFuture(upvoteResponse);
                                            }
                                        });
                            });
                }
            });
        });
    }
    public void realtime_upvote(Upvote upvote,Account account, String entity, String titleNotification){
        DataNotification dataNotification = DataNotification.builder()
                .id(upvote.getUpvoteId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(titleNotification)
                    .message(messageJson)
                    .isRead(false)
                    .account(account)
                    .createdDate(LocalDateTime.now())
                    .build();
            unitOfWork.getNotificationRepository().save(notification);
            socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.NOTIFICATION.name(), notification);
            socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.REFRESH.toString(), upvote);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void realtime_dailyPoint(DailyPoint dailyPoint,Account account, String entity, String titleNotification,UUID clientSessionId){
        DataNotification dataNotification = DataNotification.builder()
                .id(dailyPoint.getDailyPointId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(titleNotification)
                    .message(messageJson)
                    .isRead(false)
                    .account(account)
                    .createdDate(LocalDateTime.now())
                    .build();
            unitOfWork.getNotificationRepository().save(notification);
            socketIOUtil.sendEventToOneClientInAServer(clientSessionId,WebsocketEventName.NOTIFICATION.toString(), dailyPoint);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<UpvoteGetAllResponse> getAllUpvoteByPostId(UUID postId) {
        var postFuture = findPostById(postId);

        return postFuture.thenCompose(post -> {
            var upvoteListFuture = unitOfWork.getUpvoteRepository().findByPost(post);

            return upvoteListFuture.thenApply(upvoteList -> {
                var convertResponse = upvoteList.stream()
                        .map(upvoteMapper::toUpvoteNoPostResponse)
                        .toList();

                UpvoteGetAllResponse getAllResponse = new UpvoteGetAllResponse();
                getAllResponse.setCount(convertResponse.size());
                getAllResponse.setNoPostResponseList(convertResponse);

                return getAllResponse;
            });
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<UpvoteGetAllResponse> getAllUpvoteByAccountId(UUID accountId) {
        var accountFuture = findAccountById(accountId);

        return accountFuture.thenCompose(account -> {
            var upvoteListFuture = unitOfWork.getUpvoteRepository().findByAccount(account);

            return upvoteListFuture.thenApply(upvoteList -> {
                var convertResponse = upvoteList.stream()
                        .map(upvoteMapper::toUpvoteNoPostResponse)
                        .toList();

                UpvoteGetAllResponse getAllResponse = new UpvoteGetAllResponse();
                getAllResponse.setCount(convertResponse.size());
                getAllResponse.setNoPostResponseList(convertResponse);

                return getAllResponse;
            });
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<UpvoteResponse>> getAllUpvotes() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getUpvoteRepository().findAll().stream()
                        .map(upvoteMapper::toUpvoteResponse)
                        .toList());
    }


    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    public String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> createDailyPointLog(Account account, Post post, TypeBonus typeBonus) {
        return unitOfWork.getDailyPointRepository()
                .findByPostAndTypeBonus(post, typeBonus)
                .thenCompose(dailyPoint -> {
                    if (dailyPoint != null || account.getWallet() == null) {
                        return CompletableFuture.completedFuture(null);
                    }

                    DailyPoint newDailyPoint = new DailyPoint();
                    newDailyPoint.setCreatedDate(new Date());
                    newDailyPoint.setPoint(null);
                    newDailyPoint.setPost(post);
                    newDailyPoint.setAccount(account);
                    newDailyPoint.setTypeBonus(typeBonus);
                    newDailyPoint.setPointEarned(typeBonus.getPointBonus());

                    var walletFuture = addPointToWallet(account, typeBonus);
                    return walletFuture.thenApply(wallet -> {
                        if (wallet == null) {
                            System.out.println("This Account Doesn't Have Wallet. Continuing without adding points");
                        }

                        return unitOfWork.getDailyPointRepository().save(newDailyPoint);
                    });
                });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Wallet> addPointToWallet(Account account, TypeBonus typeBonus) {
        var walletFuture = unitOfWork.getWalletRepository().findByAccount(account);

        return walletFuture.thenCompose(wallet -> {
            if (wallet == null) {
                return CompletableFuture.completedFuture(null);
            }

            var balance = wallet.getBalance();
            balance = balance + typeBonus.getPointBonus();
            wallet.setBalance(balance);

            return CompletableFuture.completedFuture(unitOfWork.getWalletRepository().save(wallet));
        });
    }
}
