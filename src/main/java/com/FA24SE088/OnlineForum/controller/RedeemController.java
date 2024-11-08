package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.RedeemRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemDocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.service.RedeemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/redeem")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedeemController {
    RedeemService redeemService;

    @Operation(summary = "Đây là Api đổi thưởng", description = "Đây là Api đổi thưởng")
    @PostMapping("/create")
    public ApiResponse<RedeemResponse> create(@RequestBody RedeemRequest request){
        return ApiResponse.<RedeemResponse>builder()
                .entity(redeemService.create_2(request))
                .build();
    }

    @Operation(summary = "Xem phần thưởng đã đổi của tk đang đăng nhập", description = "Xem phần thưởng đã đổi của tk đang đăng nhập")
    @GetMapping("/my-reward")
    public ApiResponse<RedeemDocumentResponse> getMyDocument(){
        return ApiResponse.<RedeemDocumentResponse>builder()
                .entity(redeemService.getMyRewarded())
                .build();
    }




}
