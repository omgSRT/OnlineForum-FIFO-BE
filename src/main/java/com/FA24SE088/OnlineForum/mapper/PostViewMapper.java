package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostViewRequest;
import com.FA24SE088.OnlineForum.dto.response.PostViewResponse;
import com.FA24SE088.OnlineForum.entity.PostView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostViewMapper {
    PostView toPostView(PostViewRequest request);

    PostViewResponse toPostViewResponse(PostView postView);
}
