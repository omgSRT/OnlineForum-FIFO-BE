package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.PostViewRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.PostViewResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.PostViewService;
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
@RequestMapping("/post-view")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostViewController {
    PostViewService postViewService;

    @Operation(summary = "Create A New View For Post", description = "Admin, Staff, And Author Won't Be Able To Create View")
    @PostMapping(path = "/create")
    public ApiResponse<PostViewResponse> createPostView(@RequestBody @Valid PostViewRequest request){
        return postViewService.createPostView(request).thenApply(postViewResponse ->
                ApiResponse.<PostViewResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(postViewResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Post Views")
    @GetMapping(path = "/getall")
    public ApiResponse<List<PostViewResponse>> getAllPostViews(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int perPage,
                                                               @RequestParam(required = false) UUID accountId,
                                                               @RequestParam(required = false) UUID postId){
        return postViewService.getAllPostView(page, perPage, accountId, postId).thenApply(postViewResponses ->
                ApiResponse.<List<PostViewResponse>>builder()
                        .entity(postViewResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get Post View By ID")
    @GetMapping(path = "/get/{postViewId}")
    public ApiResponse<PostViewResponse> getPostViewById(@PathVariable UUID postViewId){
        return postViewService.getPostViewById(postViewId).thenApply(postViewResponse ->
                ApiResponse.<PostViewResponse>builder()
                        .entity(postViewResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Post View By ID")
    @DeleteMapping(path = "/delete/{postViewId}")
    public ApiResponse<PostViewResponse> deletePostViewById(@PathVariable UUID postViewId){
        return postViewService.deletePostViewById(postViewId).thenApply(postViewResponse ->
                ApiResponse.<PostViewResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(postViewResponse)
                        .build()
        ).join();
    }
}
