package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    Feedback toFeedback(FeedbackRequest request);

    @Mapping(source = "post.postId", target = "postId")
    @Mapping(source = "post.title", target = "postTitle")
    @Mapping(source = "post.content", target = "postContent")
    @Mapping(source = "post.createdDate", target = "postCreatedDate")
    @Mapping(source = "post.lastModifiedDate", target = "postLastModifiedDate")
    @Mapping(source = "post.status", target = "postStatus")
    FeedbackResponse toFeedbackResponse(Feedback feedback);
}
