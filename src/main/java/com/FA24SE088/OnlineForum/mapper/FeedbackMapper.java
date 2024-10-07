package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    Feedback toFeedback(FeedbackRequest request);

    @Mapping(target = "postId", source = "post.postId")
    @Mapping(target = "postTitle", source = "post.title")
    @Mapping(target = "postContent", source = "post.content")
    @Mapping(target = "postCreatedDate", source = "post.createdDate")
    @Mapping(target = "postLastModifiedDate", source = "post.lastModifiedDate")
    @Mapping(target = "postStatus", source = "post.status")
    FeedbackResponse toFeedbackResponse(Feedback feedback);
}
