package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DraftCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.DraftUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostGetByIdResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, PostFileMapper.class})
public interface PostMapper {
    Post toPost(PostCreateRequest request);

    Post toPost(DraftCreateRequest request);

    @Mapping(target = "imageList", source = "imageList")
    PostResponse toPostResponse(Post post);

    PostGetByIdResponse toPostGetByIdResponse(Post post);

    @Mapping(target = "dailyPointList", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "upvoteList", ignore = true)
    @Mapping(target = "commentList", ignore = true)
    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "reportList", ignore = true)
    void updatePost(@MappingTarget Post post, PostUpdateRequest postUpdateRequest);

    @Mapping(target = "dailyPointList", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "upvoteList", ignore = true)
    @Mapping(target = "commentList", ignore = true)
    @Mapping(target = "reportList", ignore = true)
    void updateDraft(@MappingTarget Post post, DraftUpdateRequest draftUpdateRequest);
}
