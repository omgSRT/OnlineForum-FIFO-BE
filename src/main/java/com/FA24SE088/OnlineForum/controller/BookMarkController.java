package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.BookMarkResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.service.BookMarkService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/bookmarks")
public class BookMarkController {
    BookMarkService bookMarkService;

    @Operation(summary = "Create bookmark", description = "Create a new bookmark for the current user")
    @PostMapping("/create")
    public ApiResponse<BookMarkResponse> createBookmark(@RequestBody UUID postId) {
        return ApiResponse.<BookMarkResponse>builder()
                .entity(bookMarkService.create(postId))
                .build();
    }

    @Operation(summary = "Remove bookmark", description = "Remove a bookmark for the current user")
    @DeleteMapping("/remove")
    public ApiResponse<Void> removeBookmark(@RequestBody UUID postId) {
        bookMarkService.unbookmark(postId);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "List bookmarks", description = "Get all bookmarks for the current user")
    @GetMapping("/list")
    public ApiResponse<List<PostResponse>> listBookmarks() {
        return ApiResponse.<List<PostResponse>>builder()
                .entity(bookMarkService.listBookmarks())
                .build();
    }
}
