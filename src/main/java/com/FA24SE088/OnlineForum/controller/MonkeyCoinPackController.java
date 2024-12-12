package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.MonkeyCoinPackRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.MonkeyCoinPackResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.service.MonkeyCoinPackService;
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
@RequestMapping("/monkey-coin-pack")
public class MonkeyCoinPackController {

    private final MonkeyCoinPackService monkeyCoinPackService;


    @PostMapping("/create")
    public ApiResponse<MonkeyCoinPackResponse> createPricing(@RequestBody MonkeyCoinPackRequest monkeyCoinPackRequest) {
        return ApiResponse.<MonkeyCoinPackResponse>builder()
                .entity(monkeyCoinPackService.createMonkeyCoinPack(monkeyCoinPackRequest))
                .build();
    }

    @PutMapping("/update/{pricingId}")
    public ApiResponse<MonkeyCoinPackResponse> update(@PathVariable UUID pricingId, @RequestBody MonkeyCoinPackRequest monkeyCoinPackRequest) {
        return ApiResponse.<MonkeyCoinPackResponse>builder()
                .entity(monkeyCoinPackService.updateMonkeyCoinPack(pricingId, monkeyCoinPackRequest))
                .build();
    }

    @GetMapping("/get-by-id/{id}")
    public ApiResponse<MonkeyCoinPackResponse> getPricing(@PathVariable UUID id) {
        return ApiResponse.<MonkeyCoinPackResponse>builder()
                .entity(monkeyCoinPackService.getMonkeyCoinPackById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.MONKEY_COIN_PACK_NOT_FOUND)))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<MonkeyCoinPackResponse>> getAllPricings() {
        return ApiResponse.<List<MonkeyCoinPackResponse>>builder()
                .entity(monkeyCoinPackService.getAllMonkeyCoinPack())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deletePricing(@PathVariable UUID id) {
        monkeyCoinPackService.deleteMonkeyCoinPack(id);
        return ApiResponse.<Void>builder().build();
    }
}