package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.OrderPointRequest;
import com.FA24SE088.OnlineForum.dto.request.RedeemRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.OrderPointResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemDocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.service.OrderPointService;
import com.FA24SE088.OnlineForum.service.RedeemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RestController
@RequestMapping("/order-point")
public class OrderPointController {

    private final OrderPointService orderPointService;

    @PostMapping("/create")
    public ApiResponse<OrderPointResponse> createOrderPoint(@RequestBody OrderPointRequest orderPointRequest) {
        return ApiResponse.<OrderPointResponse>builder()
                .entity(orderPointService.createOrderPoint(orderPointRequest))
                .build();
    }

    @GetMapping("/get-by-id/{id}")
    public ApiResponse<OrderPointResponse> getOrderPoint(@PathVariable UUID id) {
        return ApiResponse.<OrderPointResponse>builder()
                .entity(orderPointService.getOrderPointById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_POINT_NOT_FOUND)))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<OrderPointResponse>> getAllOrderPoints() {
        return ApiResponse.<List<OrderPointResponse>>builder()
                .entity(orderPointService.getAllOrderPoints())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteOrderPoint(@PathVariable UUID id) {
        orderPointService.deleteOrderPoint(id);
        return ApiResponse.<Void>builder().build();
    }
}
