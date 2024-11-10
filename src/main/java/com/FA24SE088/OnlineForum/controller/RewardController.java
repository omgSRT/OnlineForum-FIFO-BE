package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.RewardRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.RewardResponse;
import com.FA24SE088.OnlineForum.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<RewardResponse> create1(@RequestBody RewardRequest request) {
        return ApiResponse.<RewardResponse>builder()
                .entity(rewardService.createReward(request))
                .build();
    }

    @GetMapping("/getAll")
    public ApiResponse<List<RewardResponse>> getAll(){
        return ApiResponse.<List<RewardResponse>>builder()
                .entity(rewardService.getUnredeemedRewardsForCurrentUser())
                .build();
    }

    @Operation(summary = "Update Document", description = "Status: \n" +
            "ACTIVE,\n" +
            "    INACTIVE")
    @PutMapping("/update/{id}")
    public ApiResponse<RewardResponse> update(@PathVariable UUID id, @RequestBody RewardRequest request) {
        return ApiResponse.<RewardResponse>builder()
                .entity(rewardService.update(id, request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<RewardResponse> update(@PathVariable UUID id) {
        rewardService.deleteReward(id);
        return ApiResponse.<RewardResponse>builder()
                .build();
    }


}
