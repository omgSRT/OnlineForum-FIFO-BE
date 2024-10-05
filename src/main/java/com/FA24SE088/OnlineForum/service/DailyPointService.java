package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.DailyPointRequest;
import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.response.DailyPointResponse;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.DailyPoint;
import com.FA24SE088.OnlineForum.entity.Point;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.DailyPointMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class DailyPointService {
    final UnitOfWork unitOfWork;
    final DailyPointMapper dailyPointMapper;
    final PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<DailyPointResponse> createDailyPoint(DailyPointRequest request){
        var accountFuture = findAccountById(request.getAccountId());
        var postFuture = findPostById(request.getPostId());
        var totalPointFuture = countUserTotalPointAtAGivenDate(request.getAccountId(), new Date());
        var pointFuture = getPoint();

        return CompletableFuture.allOf(accountFuture, postFuture, totalPointFuture, pointFuture)
                .thenCompose(all -> {
                    var account = accountFuture.join();
                    var post = postFuture.join();
                    var totalPoint = totalPointFuture.join();
                    var pointList = pointFuture.join();
                    Point point;

                    if(pointList.isEmpty()){
                        throw new AppException(ErrorCode.POINT_NOT_FOUND);
                    }
                    point = pointList.get(0);
                    if(totalPoint > point.getMaxPoint()){
                        throw new AppException(ErrorCode);
                    }

                    DailyPoint newDailyPoint = dailyPointMapper.toDailyPoint(request);
                    newDailyPoint.setCreatedDate(new Date());
                    newDailyPoint.setPoint(point);
                    newDailyPoint.setPost(post);
                    newDailyPoint.setAccount(account);
                    newDailyPoint.setPointEarned(point.getPointPerPost());

                    return CompletableFuture.completedFuture(dailyPointMapper.toDailyPointResponse(newDailyPoint));
                })
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Double> countUserTotalPointAtAGivenDate(UUID accountId, Date givenDate){
        var accountFuture = findAccountById(accountId);
        return accountFuture.thenCompose(account ->
            unitOfWork.getDailyPointRepository().findByAccountAndCreatedDate(account, givenDate)
                    .thenCompose(dailyPoints -> {
                        double totalCount = 0;
                        if(dailyPoints == null || dailyPoints.isEmpty()){
                            totalCount = 0;
                        }
                        else{
                            for(DailyPoint dailyPoint : dailyPoints){
                                totalCount += dailyPoint.getPointEarned();
                            }
                        }

                        return CompletableFuture.completedFuture(totalCount);
                    })
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
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Point>> getPoint() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll().stream()
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
}
