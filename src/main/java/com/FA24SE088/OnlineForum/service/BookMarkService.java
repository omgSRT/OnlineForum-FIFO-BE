package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.BookMarkResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PostMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.BookMarkRepository;
import com.FA24SE088.OnlineForum.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class BookMarkService {
    AccountRepository accountRepository;
    BookMarkRepository bookMarkRepository;
    PostRepository postRepository;
    PostMapper postMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public BookMarkResponse addOrRemove(UUID postId) {
        Account currentUser = getCurrentUser();
        Optional<BookMark> existingBookmark = bookMarkRepository
                .findByAccountAndPost_PostId(currentUser, postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        BookMarkResponse response = new BookMarkResponse();
        if (existingBookmark.isPresent()) {
            bookMarkRepository.delete(existingBookmark.get());
            response.setMessage(SuccessReturnMessage.DELETE_SUCCESS.getMessage());
            response.setAccount(currentUser);
            response.setPost(post);

        } else {
            BookMark bookMark = new BookMark();
            bookMark.setAccount(currentUser);
            bookMark.setPost(post);
            bookMarkRepository.save(bookMark);
            response.setMessage(SuccessReturnMessage.CREATE_SUCCESS.getMessage());
            response.setAccount(currentUser);
            response.setPost(post);

        }
        return response;
    }


    public List<PostResponse> listBookmarks() {
        Account currentUser = getCurrentUser();
        List<BookMark> bookmarks = bookMarkRepository.findByAccount(currentUser);
        return bookmarks.stream()
                .map(bookMark -> {
                    Post post = bookMark.getPost();
                    int upvoteCount = post.getUpvoteList() != null ? post.getUpvoteList().size() : 0;
                    int commentCount = post.getCommentList() != null ? post.getCommentList().size() : 0;
                    PostResponse postResponse = postMapper.toPostResponse(post);
                    postResponse.setUpvoteCount(upvoteCount);
                    postResponse.setCommentCount(commentCount);
                    return postResponse;
                })
                .toList();
    }
}
