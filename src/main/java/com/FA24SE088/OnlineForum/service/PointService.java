package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PointMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class PointService {
    UnitOfWork unitOfWork;
    PointMapper pointMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PointResponse> createPoint(PointRequest request){
        var countPointDataExistFuture = CompletableFuture.supplyAsync(() ->
            unitOfWork.getPointRepository().count()
        );

        return countPointDataExistFuture.thenApply(count -> {
            if(count > 0){
                throw new AppException(ErrorCode.POINT_DATA_EXIST);
            }

            if(request.getMaxPoint() < request.getPointPerPost()){
                throw new AppException(ErrorCode.MAX_POINT_LOWER_THAN_INDIVIDUAL_POINT);
            }

            var newPoint = pointMapper.toPoint(request);
            newPoint.setPointCostPerDownload(15);
            newPoint.setPointEarnedPerDownload(2);

            return pointMapper.toPointResponse(unitOfWork.getPointRepository().save(newPoint));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<List<PointResponse>> getAllPoints() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll().stream()
                    .map(pointMapper::toPointResponse)
                    .toList());
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PointResponse> updatePoint(PointRequest request){
        var listPointDataFuture = CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll()
        );

        return listPointDataFuture.thenApply(listPointData -> {
            Point existPoint = new Point();

            if(!listPointData.isEmpty()){
                existPoint = listPointData.get(0);
            }
            else{
                throw new AppException(ErrorCode.POINT_NOT_FOUND);
            }

            pointMapper.updatePoint(existPoint, request);

            return pointMapper.toPointResponse(unitOfWork.getPointRepository().save(existPoint));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PointResponse> deletePoint(){
        var listPointDataFuture = CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll()
        );

        return listPointDataFuture.thenApply(listPointData -> {
            Point existPoint = new Point();

            if(!listPointData.isEmpty()){
                existPoint = listPointData.get(0);
            }
            else{
                throw new AppException(ErrorCode.POINT_NOT_FOUND);
            }

            unitOfWork.getPointRepository().delete(existPoint);

            return pointMapper.toPointResponse(existPoint);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<Void> managePointAfterDownload(UUID postId){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(postId);
        var pointFuture = getPoint();

        return CompletableFuture.allOf(accountFuture, postFuture, pointFuture).thenCompose(v -> {
            var accountDownloader = accountFuture.join();
            var post = postFuture.join();
            var pointList = pointFuture.join();
            var accountOwner = post.getAccount();

            //get wallets of both downloader and owner of the src code
            var walletDownloader = accountDownloader.getWallet();
            var walletOwner = accountOwner.getWallet();

            Point point;
            if(pointList.isEmpty()){
                throw new AppException(ErrorCode.POINT_NOT_FOUND);
            }
            point = pointList.get(0);

            //check current user have enough balance
            if(walletDownloader.getBalance() < point.getPointCostPerDownload()){
                throw new AppException(ErrorCode.BALANCE_NOT_SUFFICIENT_TO_DOWNLOAD);
            }

            walletDownloader.setBalance(walletDownloader.getBalance() - point.getPointCostPerDownload());
            walletOwner.setBalance(walletOwner.getBalance() + point.getPointEarnedPerDownload());

            var dailyPointFuture = createDailyPointLogForSourceOwner(accountOwner, post, point);

            return CompletableFuture.allOf(dailyPointFuture).thenCompose(voidData -> {
                var dailyPoint = dailyPointFuture.join();

                unitOfWork.getDailyPointRepository().save(dailyPoint);
                unitOfWork.getWalletRepository().save(walletDownloader);
                unitOfWork.getWalletRepository().save(walletOwner);

                return CompletableFuture.completedFuture(null);
            });
        });
    }

    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
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
    private CompletableFuture<List<Point>> getPoint() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll().stream()
                        .toList());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> createDailyPointLogForSourceOwner(Account account, Post post, Point point) {
        return unitOfWork.getDailyPointRepository().findByAccountAndPost(account, post)
                .thenCompose(existingDailyPoint -> {
                    if (existingDailyPoint != null) {
                        throw new AppException(ErrorCode.DAILY_POINT_ALREADY_EXIST);
                    }

                    DailyPoint newDailyPoint = new DailyPoint();
                    newDailyPoint.setCreatedDate(new Date());
                    newDailyPoint.setPoint(point);
                    newDailyPoint.setPost(post);
                    newDailyPoint.setAccount(account);
                    newDailyPoint.setTypeBonus(null);
                    newDailyPoint.setPointEarned(point.getPointEarnedPerDownload());

                    return CompletableFuture.supplyAsync(() -> unitOfWork.getDailyPointRepository().save(newDailyPoint));
                });
    }
}
