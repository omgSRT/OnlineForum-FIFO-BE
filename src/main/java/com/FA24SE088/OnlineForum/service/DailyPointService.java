package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.DailyPointRequest;
import com.FA24SE088.OnlineForum.dto.response.DailyPointResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.DailyPointMapper;
import com.FA24SE088.OnlineForum.repository.*;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class DailyPointService {
    DailyPointRepository dailyPointRepository;
    AccountRepository accountRepository;
    PostRepository postRepository;
    PointRepository pointRepository;
    TypeBonusRepository typeBonusRepository;
    DailyPointMapper dailyPointMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<DailyPointResponse> createDailyPoint(DailyPointRequest request) {
        var accountFuture = findAccountById(request.getAccountId());
        var postFuture = findPostById(request.getPostId());
        var typeBonusFuture = request.getTypeBonusId() != null
                ? findTypeBonusById(request.getTypeBonusId())
                : CompletableFuture.completedFuture(null);
        var totalPointFuture = countUserTotalPointAtAGivenDate(request.getAccountId(), new Date());
        var pointFuture = getPoint();

        return CompletableFuture.allOf(accountFuture, postFuture, totalPointFuture, typeBonusFuture, pointFuture)
                .thenCompose(all -> {
                    var account = accountFuture.join();
                    var post = postFuture.join();
                    var totalPoint = totalPointFuture.join();
                    var pointList = pointFuture.join();
                    var typeBonus = typeBonusFuture.join();

                    Point point;
                    if (typeBonus == null) {
                        if (pointList.isEmpty()) {
                            throw new AppException(ErrorCode.POINT_NOT_FOUND);
                        }
                        point = pointList.get(0);
                    } else {
                        point = null;
                    }

                    DailyPoint newDailyPoint = new DailyPoint();
                    newDailyPoint.setCreatedDate(new Date());
                    newDailyPoint.setPoint(point);
                    newDailyPoint.setPost(post);
                    newDailyPoint.setAccount(account);
                    newDailyPoint.setTypeBonus(typeBonus != null ? (TypeBonus) typeBonus : null);
                    if (point != null && (totalPoint + point.getPointPerPost() > point.getMaxPoint())) {
                        newDailyPoint.setPointEarned(0);
                    } else if (point != null && (totalPoint + point.getPointPerPost() <= point.getMaxPoint())) {
                        newDailyPoint.setPointEarned(point.getPointPerPost());
                    }

                    var dailyPointFuture = findDailyPointByAccountAndPost(account, post);

                    return dailyPointFuture.thenCompose(dailyPoint -> {
                        if (dailyPoint != null) {
                            throw new AppException(ErrorCode.DAILY_POINT_ALREADY_EXIST);
                        }

                        return CompletableFuture.completedFuture(dailyPointMapper.toDailyPointResponse(
                                dailyPointRepository.save(newDailyPoint)
                        ));
                    });
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<DailyPointResponse> getDailyPointById(UUID dailyPointId) {
        var dailyPointFuture = findDailyPointById(dailyPointId);

        return dailyPointFuture.thenApply(dailyPointMapper::toDailyPointResponse);
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<DailyPointResponse>> getAllDailyPoints(int page, int perPage, UUID accountId, UUID postId, String givenDate) {
        var accountFuture = accountId != null
                ? findAccountById(accountId)
                : CompletableFuture.completedFuture(null);
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(accountFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parseDate;
            try {
                parseDate = givenDate == null
                        ? null
                        : simpleDateFormat.parse(givenDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Date finalParseDate = parseDate;

            var list = dailyPointRepository.findAllByOrderByCreatedDateDesc().stream()
                    .filter(dailyPoint -> account == null || dailyPoint.getAccount().equals(account))
                    .filter(dailyPoint -> post == null || dailyPoint.getPost().equals(post))
                    .filter(dailyPoint -> {
                        Calendar createdDateCal = Calendar.getInstance();
                        createdDateCal.setTime(dailyPoint.getCreatedDate());
                        createdDateCal.set(Calendar.HOUR_OF_DAY, 0);
                        createdDateCal.set(Calendar.MINUTE, 0);
                        createdDateCal.set(Calendar.SECOND, 0);
                        createdDateCal.set(Calendar.MILLISECOND, 0);

                        if (finalParseDate == null) {
                            return true;
                        }

                        Calendar parsedDateCal = Calendar.getInstance();
                        parsedDateCal.setTime(finalParseDate);
                        parsedDateCal.set(Calendar.HOUR_OF_DAY, 0);
                        parsedDateCal.set(Calendar.MINUTE, 0);
                        parsedDateCal.set(Calendar.SECOND, 0);
                        parsedDateCal.set(Calendar.MILLISECOND, 0);

                        return createdDateCal.getTime().equals(parsedDateCal.getTime());
                    })
                    .map(dailyPointMapper::toDailyPointResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

            return CompletableFuture.completedFuture(paginatedList);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<DailyPointResponse> deleteDailyPointPoint(UUID dailyPointId) {
        var dailyPointFuture = findDailyPointById(dailyPointId);

        return CompletableFuture.allOf(dailyPointFuture)
                .thenCompose(all -> {
                    var dailyPoint = dailyPointFuture.join();

                    dailyPointRepository.delete(dailyPoint);

                    return CompletableFuture.completedFuture(dailyPoint);
                })
                .thenApply(dailyPointMapper::toDailyPointResponse);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Double> countUserTotalPointAtAGivenDate(UUID accountId, Date givenDate) {
        Calendar parsedDateCal = Calendar.getInstance();
        parsedDateCal.setTime(givenDate);
        parsedDateCal.set(Calendar.HOUR_OF_DAY, 0);
        parsedDateCal.set(Calendar.MINUTE, 0);
        parsedDateCal.set(Calendar.SECOND, 0);
        parsedDateCal.set(Calendar.MILLISECOND, 0);
        Date normalizedGivenDate = parsedDateCal.getTime();

        var accountFuture = findAccountById(accountId);
        return accountFuture.thenCompose(account ->
                dailyPointRepository.findByAccount(account)
                        .thenCompose(dailyPoints -> {
                            double totalCount = dailyPoints.stream()
                                    .filter(dailyPoint -> {
                                        Calendar createdDateCal = Calendar.getInstance();
                                        createdDateCal.setTime(dailyPoint.getCreatedDate());
                                        createdDateCal.set(Calendar.HOUR_OF_DAY, 0);
                                        createdDateCal.set(Calendar.MINUTE, 0);
                                        createdDateCal.set(Calendar.SECOND, 0);
                                        createdDateCal.set(Calendar.MILLISECOND, 0);

                                        return createdDateCal.getTime().equals(normalizedGivenDate);
                                    })
                                    .mapToDouble(DailyPoint::getPointEarned)
                                    .sum();

                            return CompletableFuture.completedFuture(totalCount);
                        })
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                postRepository.findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> findDailyPointById(UUID dailyPointId) {
        return CompletableFuture.supplyAsync(() ->
                dailyPointRepository.findById(dailyPointId)
                        .orElseThrow(() -> new AppException(ErrorCode.DAILY_POINT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Point>> getPoint() {
        return CompletableFuture.supplyAsync(() ->
                pointRepository.findAll().stream()
                        .toList());
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> findDailyPointByAccountAndPost(Account account, Post post) {
        return dailyPointRepository.findByAccountAndPost(account, post);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<TypeBonus> findTypeBonusById(UUID typeBonusId) {
        return CompletableFuture.supplyAsync(() ->
                typeBonusRepository.findById(typeBonusId)
                        .orElseThrow(() -> new AppException(ErrorCode.TYPE_BONUS_NOT_FOUND))
        );
    }
}
