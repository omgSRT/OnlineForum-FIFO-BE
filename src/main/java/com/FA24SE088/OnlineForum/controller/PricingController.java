package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.PricingRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.PricingResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.service.PricingService;
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
@RequestMapping("/pricing")
public class PricingController {

    private final PricingService pricingService;


    @PostMapping("/create")
    public ApiResponse<PricingResponse> createPricing(@RequestBody PricingRequest pricingRequest) {
        return ApiResponse.<PricingResponse>builder()
                .entity(pricingService.createPricing(pricingRequest))
                .build();
    }
    @PutMapping ("/update/{pricingId}")
    public ApiResponse<PricingResponse> update(@PathVariable UUID pricingId, @RequestBody PricingRequest pricingRequest) {
        return ApiResponse.<PricingResponse>builder()
                .entity(pricingService.updatePricing(pricingId,pricingRequest))
                .build();
    }
    @GetMapping("/get-by-id/{id}")
    public ApiResponse<PricingResponse> getPricing(@PathVariable UUID id) {
        return ApiResponse.<PricingResponse>builder()
                .entity(pricingService.getPricingById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PRICING_NOT_FOUND)))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<PricingResponse>> getAllPricings() {
        return ApiResponse.<List<PricingResponse>>builder()
                .entity(pricingService.getAllPricings())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deletePricing(@PathVariable UUID id) {
        pricingService.deletePricing(id);
        return ApiResponse.<Void>builder().build();
    }
}