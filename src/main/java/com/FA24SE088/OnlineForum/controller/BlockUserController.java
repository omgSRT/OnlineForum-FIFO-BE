package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.BlockRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.service.BlockUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/block-user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlockUserController {
    BlockUserService blockUserService;

    @Operation(summary = "Block user", description = "Block a user for the current user")
    @PostMapping("/block-or-unblock")
    public ApiResponse<BlockAccountResponse> blockUser(@RequestBody BlockRequest request) {
        return ApiResponse.<BlockAccountResponse>builder()
                .entity(blockUserService.blockOrUnblock(request.getAccountID()))
                .build();
    }

    @Operation(summary = "Get block list", description = "")
    @GetMapping("/get-list-user-block")
    public ApiResponse<List<AccountResponse>> getFollow() {
        return ApiResponse.<List<AccountResponse>>builder()
                .entity(blockUserService.listBlock())
                .build();
    }

}
