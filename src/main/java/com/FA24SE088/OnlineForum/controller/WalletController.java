package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.WalletRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.AccountService;
import com.FA24SE088.OnlineForum.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/wallet")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletController {
    final WalletService walletService;
    @PostMapping("/create")
    public ApiResponse<Wallet> create(@RequestBody WalletRequest request) {
        return ApiResponse.<Wallet>builder()
                .entity(walletService.create(request))
                .build();
    }

    @GetMapping("/getWallet")
    public ApiResponse<Wallet> getAll(@RequestParam UUID uuid) {
        return ApiResponse.<Wallet>builder()
                .entity(walletService.getByID(uuid))
                .build();
    }

}
