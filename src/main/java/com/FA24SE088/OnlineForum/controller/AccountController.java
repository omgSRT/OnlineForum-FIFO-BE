package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.entity.OrderPoint;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.enums.OrderPointStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateCategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateInfoRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.RecommendAccountResponse;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.enums.RoleAccount;
import com.FA24SE088.OnlineForum.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/account")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountController {
    AccountService accountService;



    @Operation(summary = "Find Account", description = "Find By Username")
    @GetMapping(path = "/find/by-username")
    public ApiResponse<Account> findByUsername(@NotNull String username) {
        return ApiResponse.<Account>builder()
                .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                .entity(accountService.findByUsername(username))
                .build();
    }

    @GetMapping("/login/google")
    public ApiResponse<String> loginGG() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?response_type=code&client_id=376166376344-tqh1arjjec1n55khfkv9mosg882bgn7o.apps.googleusercontent.com&scope=email%20profile&state=ua-Zwcfy0imSTBXTkwGv-Yb-bO6wNHzwJlPqsfRWQwk%3D&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin%2Foauth2%2Fcode%2Fgoogle&service=lso&o2v=2&ddm=1&flowName=GeneralOAuthFlow";
        return ApiResponse.<String>builder()
                .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                .entity(url)
                .build();
    }
    

    @Operation(summary = "Find Account", description = "Find By Username Contain Any Letter")
    @GetMapping(path = "/list/find/by-username")
    public ApiResponse<List<Account>> findByUsernameContainingAsync(@NotNull String username) {
        return accountService.findByUsernameContainingAsync(username).thenApply(accounts ->
                ApiResponse.<List<Account>>builder()
                        .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                        .entity(accounts)
                        .build()
        ).join();
    }

    @GetMapping("/callback")
    public ApiResponse<AccountResponse> callbackLoginGoogle(@AuthenticationPrincipal OAuth2User oAuth2User) {
        AccountResponse response = accountService.callbackLoginGoogle(oAuth2User);
        return ApiResponse.<AccountResponse>builder()
                .message(SuccessReturnMessage.LOGIN_SUCCESS.getMessage())
                .entity(response)
                .build();
    }

    @Operation(summary = "Get Recommended Accounts", description = "Get Accounts Based On Last Activities From 48 Hours Ago")
    @GetMapping(path = "/get/recommended")
    public ApiResponse<List<RecommendAccountResponse>> getRecommendedAccounts(@RequestParam(defaultValue = "1") int page,
                                                                              @RequestParam(defaultValue = "10") int perPage) {
        return accountService.getRecommendedAccounts(page, perPage).thenApply(recommendAccountResponses ->
                ApiResponse.<List<RecommendAccountResponse>>builder()
                        .entity(recommendAccountResponses)
                        .build()
        ).join();
    }

    @PutMapping("/update-info")
    public ApiResponse<AccountResponse> updateInfo(@RequestBody AccountUpdateInfoRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .entity(accountService.updateInfo(request))
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<List<AccountResponse>> getAll(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int perPage,
                                                     @RequestParam(required = false) String username,
                                                     @RequestParam(required = false) String email,
                                                     @RequestParam(required = false, defaultValue = "ACTIVE") AccountStatus status,
                                                     @RequestParam(required = false) RoleAccount role) {

        return ApiResponse.<List<AccountResponse>>builder()
                .entity(accountService.filter(page, perPage, username, email, status, role))
                .build();
    }


    @GetMapping("/get-by-id/{id}")
    public ApiResponse<AccountResponse> findById(@PathVariable UUID id) {
        return ApiResponse.<AccountResponse>builder()
                .entity(accountService.findById(id))
                .build();
    }

    @PutMapping("/update-category-for-staff/{id}")
    public ApiResponse<AccountResponse> updateCategory(@PathVariable UUID id, AccountUpdateCategoryRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .entity(accountService.updateCategoryOfStaff(id, request))
                .build();
    }

//    @PutMapping("/ban-unban/{accountId}")
//    public ApiResponse<AccountResponse> banAndUnban(@PathVariable UUID accountId){
//        return ApiResponse.<AccountResponse>builder()
//                .entity(accountService.banAccount(accountId))
//                .build();
//    }

    @Operation(summary = "Delete Account", description = "Delete Account By ID")
    @DeleteMapping(path = "/delete/{accountId}")
    public ApiResponse<Account> deleteAccount(@PathVariable UUID accountId) {
        return accountService.delete(accountId).thenApply(account ->
                ApiResponse.<Account>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(account)
                        .build()
        ).join();
    }

}
