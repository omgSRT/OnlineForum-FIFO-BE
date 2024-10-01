package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    final AccountService accountService;

    @Operation(summary = "Find Account", description = "Find By Username")
    @GetMapping(path = "/find/by-username")
    public ApiResponse<Account> findByUsername(@NotNull String username){
        return ApiResponse.<Account>builder()
                .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                .entity(accountService.findByUsername(username))
                .build();
    }
}
