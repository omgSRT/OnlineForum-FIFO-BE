package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.response.UpvoteCreateDeleteResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteNoPostResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteResponse;
import com.FA24SE088.OnlineForum.entity.Upvote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpvoteMapper {
    UpvoteResponse toUpvoteResponse(Upvote upvote);

    UpvoteCreateDeleteResponse toUpvoteCreateDeleteResponse(Upvote upvote);

    UpvoteNoPostResponse toUpvoteNoPostResponse(Upvote upvote);
}
