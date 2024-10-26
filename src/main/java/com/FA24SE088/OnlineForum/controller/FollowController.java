package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.BlockRequest;
import com.FA24SE088.OnlineForum.dto.request.UnfollowRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.FollowResponse;

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
    @PostMapping("/create")
    public ApiResponse<FollowResponse> createFollow(@RequestBody UUID id) {
        return ApiResponse.<FollowResponse>builder()
                .entity(followService.create(id))
                .build();
    }

    @Operation(summary = "Block user", description = "Block a user for the current user")
    @PostMapping("/block")
    public ApiResponse<Void> blockUser(@RequestBody BlockRequest request) {
        followService.blockUser(request.getAccountID());
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Unblock user", description = "Block a user for the current user")
    @DeleteMapping("/unblock")
    public ApiResponse<Void> unblockUser(@RequestBody BlockRequest request) {
        followService.unblock(request.getAccountID());
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/unfollow")
    public ApiResponse<Void> unfollow(@RequestBody UnfollowRequest request) {
        followService.unfollow(request);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Get block list", description = "")
    @GetMapping("/get-list-user-block")
    public ApiResponse<List<AccountResponse>> getFollow() {
        return ApiResponse.<List<AccountResponse>>builder()
                .entity(followService.listBlock())
                .build();
    }

    @Operation(summary = "Danh sách người mình đang follow", description = "")
    @GetMapping("/get-follows")
    public ApiResponse<List<FollowResponse>> getAllFollows() {
        return ApiResponse.<List<FollowResponse>>builder()
                .entity(followService.getFollows())
                .build();
    }

    @Operation(summary = "Danh sách người đang follow mình", description = "")
    @GetMapping("get-followers")
    public ApiResponse<List<FollowResponse>> getAllFollowers() {
        return ApiResponse.<List<FollowResponse>>builder()
                .entity(followService.getFollowers())
                .build();
    }

}
