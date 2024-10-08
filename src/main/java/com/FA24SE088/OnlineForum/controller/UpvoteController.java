package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.UpvoteRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteCreateDeleteResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteResponse;
import com.FA24SE088.OnlineForum.service.UpvoteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/upvote")
@Slf4j
public class UpvoteController {
    final UpvoteService upvoteService;

    @Operation(summary = "Add Or Remove Upvote From A Post")
    @PostMapping(path = "/add-or-delete")
    public ApiResponse<UpvoteCreateDeleteResponse> createOrDeleteUpvote(@RequestBody @Valid UpvoteRequest request){
        return upvoteService.addOrDeleteUpvote(request).thenApply(upvoteCreateDeleteResponse ->
                ApiResponse.<UpvoteCreateDeleteResponse>builder()
                        .entity(upvoteCreateDeleteResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Upvotes", description = "Get All Upvote By Post")
    @GetMapping(path = "/getall/by-post/{postId}")
    public ApiResponse<List<UpvoteResponse>> getAllUpvotesByPost(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int perPage,
                                                                 @PathVariable UUID postId){
        return upvoteService.getAllUpvoteByPostId(page, perPage, postId).thenApply(upvoteResponses ->
                ApiResponse.<List<UpvoteResponse>>builder()
                        .entity(upvoteResponses)
                        .build()
        ).join();
    }
}
