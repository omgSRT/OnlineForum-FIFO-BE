package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.BookMarkResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.BookMarkMapper;
import com.FA24SE088.OnlineForum.mapper.PostMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class BookMarkService {
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    BookMarkMapper bookMarkMapper;
    PostMapper postMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public BookMarkResponse create(UUID postId) {
        Account currentUser = getCurrentUser();
        Optional<BookMark> existingBookmark = unitOfWork.getBookMarkRepository()
                .findByAccountAndPost_PostId(currentUser, postId);

        if (existingBookmark.isPresent()) {
            throw new AppException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        BookMark bookMark = new BookMark();
        bookMark.setAccount(currentUser);
        bookMark.setPost(unitOfWork.getPostRepository().findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)));

        BookMark savedBookMark = unitOfWork.getBookMarkRepository().save(bookMark);
        return bookMarkMapper.toResponse(savedBookMark);
    }
    public void unbookmark(UUID postId) {
        Account currentUser = getCurrentUser();

        BookMark bookMark = unitOfWork.getBookMarkRepository()
                .findByAccountAndPost_PostId(currentUser, postId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKMARK_NOT_FOUND));

        unitOfWork.getBookMarkRepository().delete(bookMark);
    }

    public List<PostResponse> listBookmarks() {
        Account currentUser = getCurrentUser();
        List<BookMark> bookmarks = unitOfWork.getBookMarkRepository().findByAccount(currentUser);
//        return bookmarks.stream()
//                .map(BookMark::getPost) // Lấy Post từ mỗi BookMark
//                .toList();
//    }
        return bookmarks.stream()
                .map(bookMark -> {
                    Post post = bookMark.getPost();
                    return postMapper.toPostResponse(post);
                })
                .toList();
    }

}
