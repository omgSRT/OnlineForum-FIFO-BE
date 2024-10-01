package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.requests.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/account")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
    @PostMapping("/create")
    public ApiResponse<AccountResponse> create(@RequestBody AccountRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .entity(accountService.create(request))
                .build();
    }
}
