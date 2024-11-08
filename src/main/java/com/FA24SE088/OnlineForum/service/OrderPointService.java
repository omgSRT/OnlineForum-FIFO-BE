package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.OrderPointRequest;
import com.FA24SE088.OnlineForum.dto.response.OrderPointResponse;
import com.FA24SE088.OnlineForum.entity.OrderPoint;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.OrderPointMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class OrderPointService {

    UnitOfWork unitOfWork;
    OrderPointMapper orderPointMapper;

    public OrderPointResponse createOrderPoint(OrderPointRequest orderPointRequest) {
        OrderPoint orderPoint = orderPointMapper.toOrderPoint(orderPointRequest);
        OrderPoint savedOrderPoint = unitOfWork.getOrderPointRepository().save(orderPoint);
        return orderPointMapper.toOderPointResponse(savedOrderPoint);
    }

    public Optional<OrderPointResponse> getOrderPointById(UUID orderPointId) {
        return unitOfWork.getOrderPointRepository()
                .findById(orderPointId)
                .map(orderPointMapper::toOderPointResponse);
    }

    public List<OrderPointResponse> getAllOrderPoints() {
        List<OrderPoint> orderPoints = unitOfWork.getOrderPointRepository().findAll();
        return orderPoints.stream().map(orderPointMapper::toOderPointResponse).toList();
    }

    public void deleteOrderPoint(UUID orderPointId) {
        if (unitOfWork.getOrderPointRepository().existsById(orderPointId)) {
            unitOfWork.getOrderPointRepository().deleteById(orderPointId);
        } else {
            throw new AppException(ErrorCode.ORDER_POINT_NOT_FOUND);
        }
    }
}

