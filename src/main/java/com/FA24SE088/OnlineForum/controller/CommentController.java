package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.CommentCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.CommentGetAllResponse;
import com.FA24SE088.OnlineForum.dto.request.CommentUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReplyCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.CommentNoPostResponse;
import com.FA24SE088.OnlineForum.dto.response.CommentResponse;
import com.FA24SE088.OnlineForum.dto.response.ReplyCreateResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.CommentService;
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
@RequestMapping("/comment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentController {
    CommentService commentService;

    @Operation(summary = "Create New Comment")
    @PostMapping(path = "/create")
    public ApiResponse<CommentResponse> createComment(@RequestBody @Valid CommentCreateRequest request){
        return commentService.createComment(request).thenApply(commentResponse ->
                ApiResponse.<CommentResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(commentResponse)
                        .build()
        ).join();
    }
    @Operation(summary = "Create New Reply")
    @PostMapping(path = "/create/reply")
    public ApiResponse<ReplyCreateResponse> createReply(@RequestBody @Valid ReplyCreateRequest request){
        return commentService.createReply(request).thenApply(replyCreateResponse ->
                ApiResponse.<ReplyCreateResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(replyCreateResponse)
                        .build()
        ).join();
    }
    @Operation(summary = "Get All Comments")
    @GetMapping(path = "/getall")
    public ApiResponse<List<CommentGetAllResponse>> getAllComments(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int perPage){
        return commentService.getAllComments(page, perPage).thenApply(
                commentResponses ->
                        ApiResponse.<List<CommentGetAllResponse>>builder()
                                .entity(commentResponses)
                                .build()
        ).join();
    }
    @Operation(summary = "Get All Comments", description = "Get All Comments By Post")
    @GetMapping(path = "/getall/by-post/{postId}")
    public ApiResponse<List<CommentNoPostResponse>> getAllCommentsByPost(@RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "10") int perPage,
                                                                         @PathVariable UUID postId){
        return commentService.getAllCommentsByPost(page, perPage, postId).thenApply(
                commentNoPostResponses ->
                        ApiResponse.<List<CommentNoPostResponse>>builder()
                                .entity(commentNoPostResponses)
                                .build()
        ).join();
    }
    @Operation(summary = "Get All Comments", description = "Get All Comments By Account")
    @GetMapping(path = "/getall/by-account/{accountId}")
    public ApiResponse<List<CommentResponse>> getAllCommentsByAccount(@RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "10") int perPage,
                                                                         @PathVariable UUID accountId){
        return commentService.getAllCommentsByAccount(page, perPage, accountId).thenApply(
                commentResponses ->
                        ApiResponse.<List<CommentResponse>>builder()
                                .entity(commentResponses)
                                .build()
        ).join();
    }
    @Operation(summary = "Update Comment", description = "Update Comment By ID")
    @PutMapping(path = "/update/{commentId}")
    public ApiResponse<CommentResponse> updateComment(@PathVariable UUID commentId,
                                                      @RequestBody @Valid CommentUpdateRequest request){
        return commentService.updateComment(commentId, request).thenApply(
                commentResponse ->
                        ApiResponse.<CommentResponse>builder()
                                .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                                .entity(commentResponse)
                                .build()
        ).join();
    }
    @Operation(summary = "Delete Comment", description = "Delete Comment By User")
    @DeleteMapping(path = "/delete/by-user/{commentId}")
    public ApiResponse<CommentResponse> deleteCommentForUser(@PathVariable UUID commentId){
        return commentService.deleteCommentForUser(commentId).thenApply(
                commentResponse ->
                        ApiResponse.<CommentResponse>builder()
                                .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                                .entity(commentResponse)
                                .build()
        ).join();
    }
    @Operation(summary = "Delete Comment", description = "Delete Comment By Admin Or Staff")
    @DeleteMapping(path = "/delete/by-admin-or-staff/{commentId}")
    public ApiResponse<CommentResponse> deleteCommentForAdminOrStaff(@PathVariable UUID commentId){
        return commentService.deleteCommentForAdminAndStaff(commentId).thenApply(
                commentResponse ->
                        ApiResponse.<CommentResponse>builder()
                                .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                                .entity(commentResponse)
                                .build()
        ).join();
    }
}
