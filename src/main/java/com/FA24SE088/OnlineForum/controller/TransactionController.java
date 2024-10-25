package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.TransactionRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.TransactionResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transaction")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionController {
    TransactionService transactionService;

    @Operation(summary = "Create Transaction")
    @PostMapping(path = "/create")
    public ApiResponse<TransactionResponse> createTransaction(@RequestBody @Valid TransactionRequest request,
                                                              @RequestParam TransactionType type){
        return transactionService.createTransaction(request, type).thenApply(transactionResponse ->
            ApiResponse.<TransactionResponse>builder()
                    .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                    .entity(transactionResponse)
                    .build()
        ).join();
    }

    @Operation(summary = "Get All Transaction")
    @GetMapping(path = "/getall")
    public ApiResponse<List<TransactionResponse>> getAllTransactions(@RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "10") int perPage,
                                                                     @RequestParam(required = false) UUID accountId,
                                                                     @RequestParam(required = false) String givenDate){
        return transactionService.getAllTransaction(page, perPage, accountId, givenDate).thenApply(transactionResponses ->
                ApiResponse.<List<TransactionResponse>>builder()
                        .entity(transactionResponses)
                        .build()
        ).join();
    }

    @Operation(description = "Get Transaction", summary = "Get Transaction By ID")
    @GetMapping(path = "/get/{transactionId}")
    public ApiResponse<TransactionResponse> getTransactionById(@PathVariable UUID transactionId){
        return transactionService.getTransactionById(transactionId).thenApply(transactionResponse ->
                ApiResponse.<TransactionResponse>builder()
                        .entity(transactionResponse)
                        .build()
        ).join();
    }

    @Operation(description = "Delete Transaction", summary = "Delete Transaction By ID")
    @DeleteMapping(path = "/delete/{transactionId}")
    public ApiResponse<TransactionResponse> deleteTransactionById(@PathVariable UUID transactionId){
        return transactionService.deleteTransaction(transactionId).thenApply(transactionResponse ->
                ApiResponse.<TransactionResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(transactionResponse)
                        .build()
        ).join();
    }
}
