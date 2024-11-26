package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.DoDResponse;
import com.FA24SE088.OnlineForum.entity.OrderPoint;
import com.FA24SE088.OnlineForum.mapper.*;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class StatisticService {
    UnitOfWork unitOfWork;
    RedisTemplate<String, Long> redisTemplate;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<DoDResponse> getDodStatistic(){
        var countAccountFuture = countAllAccounts();
        var countPostFuture = countAllPosts();
        var countUpvoteFuture = countAllUpvotes();
        var countCommentFuture = countAllComments();
        var countPostViewFuture = countAllPostViews();
        var countMoneyFuture = countAllMoneyInOrderPoints();

        return CompletableFuture.allOf(countAccountFuture, countPostFuture, countUpvoteFuture,
                countCommentFuture, countPostViewFuture, countMoneyFuture).thenCompose(v -> {
            var countAccount = countAccountFuture.join();
            var countPost = countPostFuture.join();
            var countUpvote = countUpvoteFuture.join();
            var countComment = countCommentFuture.join();
            var countPostView = countPostViewFuture.join();
            var countMoney = countMoneyFuture.join();
            var countActivity = countUpvote + countComment + countPostView;

            var countAccountRedis = redisTemplate.opsForValue().get("countAccountRedis");
            countAccountRedis = (countAccountRedis != null) ? countAccountRedis : 0L;

            var countPostRedis = redisTemplate.opsForValue().get("countPostRedis");
            countPostRedis = (countPostRedis != null) ? countPostRedis : 0L;

            var countActivityRedis = redisTemplate.opsForValue().get("countActivityRedis");
            countActivityRedis = (countActivityRedis != null) ? countActivityRedis : 0L;

            var countMoneyRedis = redisTemplate.opsForValue().get("countMoneyRedis");
            countMoneyRedis = (countMoneyRedis != null) ? countMoneyRedis : 0L;

            double accountGrowthRate = (countAccountRedis != 0)
                    ? (double) ((countAccount - countAccountRedis) / countAccountRedis) * 100
                    : 0;
            accountGrowthRate = formatToTwoDecimalPlaces(accountGrowthRate);
            double postGrowthRate = (countPostRedis != 0)
                    ? (double) ((countPost - countPostRedis) / countPostRedis) * 100
                    : 0;
            postGrowthRate = formatToTwoDecimalPlaces(postGrowthRate);
            double activityGrowthRate = (countActivity != 0)
                    ? (double) ((countActivity - countActivityRedis) / countActivity) * 100
                    : 0;
            activityGrowthRate = formatToTwoDecimalPlaces(activityGrowthRate);
            double moneyGrowthRate = (countMoneyRedis != 0)
                    ? (double) ((countMoney - countMoneyRedis) / countMoneyRedis) * 100
                    : 0;
            moneyGrowthRate = formatToTwoDecimalPlaces(moneyGrowthRate);

            DoDResponse dodResponse = DoDResponse.builder()
                    .accountAmount(countAccount)
                    .accountGrowthRate(accountGrowthRate)
                    .postAmount(countPost)
                    .postGrowthRate(postGrowthRate)
                    .activityAmount(countActivity)
                    .activityGrowthRate(activityGrowthRate)
                    .depositAmount(countMoney)
                    .depositGrowthRate(moneyGrowthRate)
                    .build();

            redisTemplate.opsForValue().set("countAccountRedis", countAccount, Duration.ofDays(1));
            redisTemplate.opsForValue().set("countPostRedis", countPost, Duration.ofDays(1));
            redisTemplate.opsForValue().set("countActivityRedis", countActivity, Duration.ofDays(1));
            redisTemplate.opsForValue().set("countMoneyRedis", countMoney, Duration.ofDays(1));

            return CompletableFuture.completedFuture(dodResponse);
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Long> countAllAccounts(){
        return CompletableFuture.supplyAsync(() -> unitOfWork.getAccountRepository().count());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Long> countAllPosts(){
        return CompletableFuture.supplyAsync(() -> unitOfWork.getPostRepository().count());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Long> countAllUpvotes(){
        return CompletableFuture.supplyAsync(() -> unitOfWork.getUpvoteRepository().count());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Long> countAllComments(){
        return CompletableFuture.supplyAsync(() -> unitOfWork.getCommentRepository().count());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Long> countAllPostViews(){
        return CompletableFuture.supplyAsync(() -> unitOfWork.getPostViewRepository().count());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Long> countAllMoneyInOrderPoints(){
        return CompletableFuture.supplyAsync(() -> {
            long amount = 0L;
            var orderPointList = unitOfWork.getOrderPointRepository().findAll();
            if(orderPointList.isEmpty()){
                return amount;
            }

            for(OrderPoint orderPoint : orderPointList){
                var moneyAmount = orderPoint.getAmount();
                amount += (long) moneyAmount;
            }

            return amount;
        });
    }
    private double formatToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
