package com.FA24SE088.OnlineForum.mapper;


import com.FA24SE088.OnlineForum.dto.response.FollowOrUnfollowResponse;
import com.FA24SE088.OnlineForum.dto.response.FollowResponse;

import com.FA24SE088.OnlineForum.entity.Follow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    FollowResponse toRespone(Follow follow);
    FollowOrUnfollowResponse toResponse2(Follow follow);
}
