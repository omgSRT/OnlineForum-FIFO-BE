package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.FollowUnfollowRequest;
import com.FA24SE088.OnlineForum.dto.response.*;

import com.FA24SE088.OnlineForum.service.FollowService;
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
@RequestMapping("/follow")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FollowController {
    FollowService followService;

    @Operation(summary = "Create follow", description = "Create a new follow for the current user")
    @PostMapping("/follow-or-unfollow")
    public ApiResponse<FollowOrUnfollowResponse> createFollow(@RequestBody FollowUnfollowRequest request) {
        return ApiResponse.<FollowOrUnfollowResponse>builder()
                .entity(followService.followOrUnfollow(request.getAccountId()))
                .build();
    }

    @Operation(summary = "Danh sách người mình đang follow", description = "")
    @GetMapping("/get-follows")
//    public ApiResponse<List<FollowResponse>> getAllFollows() {
//        return ApiResponse.<List<FollowResponse>>builder()
//                .entity(followService.getFollows())
//                .build();
//    }
    public ApiResponse<List<AccountForFollowedResponse>> getAllFollows() {
        return ApiResponse.<List<AccountForFollowedResponse>>builder()
                .entity(followService.getFollows())
                .build();
    }
    @GetMapping("/get-another-follows/{accountId}")
    public ApiResponse<List<AccountForFollowedResponse>> getAllFollowsAnotherAccount(@PathVariable UUID accountId) {
        return ApiResponse.<List<AccountForFollowedResponse>>builder()
                .entity(followService.getFollowsAnotherAccount(accountId))
                .build();
    }

    @Operation(summary = "Danh sách người đang follow mình", description = "")
    @GetMapping("get-followers")
//    public ApiResponse<List<FollowResponse>> getAllFollowers() {
//        return ApiResponse.<List<FollowResponse>>builder()
//                .entity(followService.getFollowers())
//                .build();
//    }
    public ApiResponse<List<AccountForFollowedResponse>> getAllFollowers() {
        return ApiResponse.<List<AccountForFollowedResponse>>builder()
                .entity(followService.getFollowers())
                .build();
    }
    @GetMapping("get-another-followers/{accountId}")
    public ApiResponse<List<AccountForFollowedResponse>> getAllFollowersAnotherAccount(@PathVariable UUID accountId) {
        return ApiResponse.<List<AccountForFollowedResponse>>builder()
                .entity(followService.getFollowersAnotherAccount(accountId))
                .build();
    }

    @GetMapping("/top-followed-accounts")
    public ApiResponse<List<AccountFollowResponse>> getTop10MostFollowedAccounts() {
        return ApiResponse.<List<AccountFollowResponse>>builder()
                .entity(followService.getTop10MostFollowedAccounts())
                .build();
    }

}
