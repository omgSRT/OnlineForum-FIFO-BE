package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RedeemMapper {
    Post toPost(PostCreateRequest request);

    PostResponse toPostResponse(Post post);
}
