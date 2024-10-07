package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateCategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.RedeemRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Redeem;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.AccountService;
import com.FA24SE088.OnlineForum.service.RedeemService;
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
@RequestMapping("/redeem")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedeemController {
    final RedeemService redeemService;

    @PostMapping("/create")
    public ApiResponse<Redeem> create(@RequestBody RedeemRequest request){
        return ApiResponse.<Redeem>builder()
                .entity(redeemService.create(request))
                .build();
    }




}
