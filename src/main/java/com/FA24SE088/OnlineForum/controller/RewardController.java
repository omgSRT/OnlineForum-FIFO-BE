package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.RewardRequest;
import com.FA24SE088.OnlineForum.dto.request.RewardUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.RewardResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.service.RewardService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reward")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RewardController {
    RewardService rewardService;

    @PostMapping("/create-reward")
    public ApiResponse<RewardResponse> create1(@RequestBody @Validated RewardRequest request) {
        return ApiResponse.<RewardResponse>builder()
                .entity(rewardService.create(request))
                .build();
    }

    @GetMapping("/getAll")
    public ApiResponse<List<RewardResponse>> getAll() {
        return ApiResponse.<List<RewardResponse>>builder()
                .entity(rewardService.getUnredeemedRewardsForCurrentUser())
                .build();
    }

    //    @GetMapping("/{rewardId}/download")
//    public ApiResponse<byte[]> downloadFileSourceCode(@PathVariable UUID rewardId) {
//        return ApiResponse.<byte[]>builder()
//                .entity(rewardService.downloadFileSourceCode(rewardId))
//                .build();
//    }
    @GetMapping("/{rewardId}/download")
    @Operation(summary = "Download Source Code for Reward")
    public ApiResponse<String> downloadFileSourceCode(@PathVariable UUID rewardId) {
        byte[] fileBytes = rewardService.downloadFileSourceCode(rewardId);

        if (fileBytes == null || fileBytes.length == 0) {
            throw new AppException(ErrorCode.NO_FILES_TO_DOWNLOAD);
        }
        String byteRepresentation = Arrays.toString(fileBytes);

        return ApiResponse.<String>builder()
                .entity(byteRepresentation)
                .build();
    }

    @GetMapping("/getAll/admin")
    public ApiResponse<List<RewardResponse>> getAllAdmin() {
        return ApiResponse.<List<RewardResponse>>builder()
                .entity(rewardService.getAll())
                .build();
    }

    @GetMapping("/get/{rewardId}")
    public ApiResponse<RewardResponse> getById(@PathVariable UUID rewardId) {
        return ApiResponse.<RewardResponse>builder()
                .entity(rewardService.getById(rewardId))
                .build();
    }

    @GetMapping("/get-all/for-current-user")
    public ApiResponse<List<RewardResponse>> getAllByCurrentUser() {
        return ApiResponse.<List<RewardResponse>>builder()
                .entity(rewardService.getAllRewardOfCurrentUser())
                .build();
    }

    @Operation(summary = "Update Document", description = "Status: \n" +
            "ACTIVE,\n" +
            "    INACTIVE")
    @PutMapping("/update/{id}")
    public ApiResponse<RewardResponse> update(@PathVariable UUID id, @RequestBody @Valid RewardUpdateRequest request) {
        return ApiResponse.<RewardResponse>builder()
                .entity(rewardService.update(id, request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<RewardResponse> delete(@PathVariable UUID id) {
        rewardService.deleteReward(id);
        return ApiResponse.<RewardResponse>builder()
                .build();
    }


}
