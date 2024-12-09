package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.UpvoteRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteCreateDeleteResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteGetAllResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteResponse;
import com.FA24SE088.OnlineForum.service.UpvoteService;
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
@RequestMapping("/upvote")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UpvoteController {
    UpvoteService upvoteService;

    @Operation(summary = "Add Or Remove Upvote From A Post")
    @PostMapping(path = "/add-or-delete")
    public ApiResponse<UpvoteCreateDeleteResponse> createOrDeleteUpvote(@RequestBody @Valid UpvoteRequest request) {
        return upvoteService.addOrDeleteUpvote(request).thenApply(upvoteCreateDeleteResponse ->
                ApiResponse.<UpvoteCreateDeleteResponse>builder()
                        .entity(upvoteCreateDeleteResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Upvotes")
    @GetMapping(path = "/getall")
    public ApiResponse<List<UpvoteResponse>> getAllUpvotes() {
        return upvoteService.getAllUpvotes().thenApply(allUpvotes ->
                ApiResponse.<List<UpvoteResponse>>builder()
                        .entity(allUpvotes)
                        .build()
        ).join();
    }
}
