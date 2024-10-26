package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicResponse;
import com.FA24SE088.OnlineForum.entity.Tag;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TagMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class TagService {
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    TagMapper tagMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TagResponse> createTag(TagRequest request){
        return unitOfWork.getTagRepository().existsByName(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    Tag newTag = tagMapper.toTag(request);
                    if(request.getBackgroundColorHex() == null || request.getBackgroundColorHex().trim().isBlank()){
                        newTag.setBackgroundColorHex("#4169E1");
                    }
                    if(request.getTextColorHex() == null || request.getTextColorHex().trim().isBlank()){
                        newTag.setTextColorHex("#FFFFFF");
                    }

                    return CompletableFuture.completedFuture(
                            tagMapper.toTagResponse(unitOfWork.getTagRepository().save(newTag))
                    );
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TagResponse>> getAllTags(int page, int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            var list = unitOfWork.getTagRepository().findAll().stream()
                    .map(tagMapper::toTagResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TagResponse>> getAllTagsByFilteringNameAndColor(int page, int perPage, String name,
                                                                                  String targetColorHex) {
        return CompletableFuture.supplyAsync(() -> {
            final int[] targetRgb = (targetColorHex != null) ? hexToRgb(targetColorHex) : null;

            List<TagResponse> list = unitOfWork.getTagRepository().findAll().stream()
                    .filter(tag -> name == null || tag.getName().contains(name))
                    .filter(tag -> {
                        if (targetRgb == null) {
                            return true;
                        }
                        String backgroundColorHex = tag.getBackgroundColorHex();
                        String textColorHex = tag.getTextColorHex();
                        return (backgroundColorHex != null && colorDistance(targetRgb, hexToRgb(backgroundColorHex)) <= 50)
                                || (textColorHex != null && colorDistance(targetRgb, hexToRgb(textColorHex)) <= 50);
                    })
                    .map(tagMapper::toTagResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    private int[] hexToRgb(String hexCode) {
        if(hexCode == null || hexCode.isBlank()){
            return null;
        }
        if (!hexCode.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
            throw new AppException(ErrorCode.INVALID_HEX_FORMAT);
        }
        if (hexCode.length() == 7) { // e.g., #RRGGBB
            return new int[] {
                    Integer.valueOf(hexCode.substring(1, 3), 16),  // Red
                    Integer.valueOf(hexCode.substring(3, 5), 16),  // Green
                    Integer.valueOf(hexCode.substring(5, 7), 16)   // Blue
            };
        } else { // Handle 3-digit hex code, e.g., #RGB
            String r = hexCode.substring(1, 2);
            String g = hexCode.substring(2, 3);
            String b = hexCode.substring(3, 4);
            return new int[] {
                    Integer.valueOf(r + r, 16),
                    Integer.valueOf(g + g, 16),
                    Integer.valueOf(b + b, 16)
            };
        }
    }
    private double colorDistance(int[] rgb1, int[] rgb2) {
        return Math.sqrt(Math.pow(rgb1[0] - rgb2[0], 2) +
                Math.pow(rgb1[1] - rgb2[1], 2) +
                Math.pow(rgb1[2] - rgb2[2], 2));
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TagResponse> getTagById(UUID tagId) {
        return CompletableFuture.supplyAsync(() -> {
            var tag = unitOfWork.getTagRepository().findById(tagId)
                    .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

            return tagMapper.toTagResponse(tag);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TagResponse> deleteTagById(UUID tagId){
        return CompletableFuture.supplyAsync(() -> {
            var tag = unitOfWork.getTagRepository().findById(tagId)
                    .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

            unitOfWork.getTagRepository().delete(tag);

            return tagMapper.toTagResponse(tag);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TagResponse> updateTagById(UUID tagId, TagRequest request) {
        return unitOfWork.getTagRepository().existsByName(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    return CompletableFuture.supplyAsync(() -> {
                        var tag = unitOfWork.getTagRepository().findById(tagId)
                                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

                        tagMapper.updateTag(tag, request);
                        return unitOfWork.getTagRepository().save(tag);
                    }).thenApply(tagMapper::toTagResponse);
                });
    }
}
