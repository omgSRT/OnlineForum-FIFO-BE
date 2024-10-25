package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.entity.Point;
import com.FA24SE088.OnlineForum.entity.Tag;
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
import org.springframework.stereotype.Service;

import java.util.List;
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
}
