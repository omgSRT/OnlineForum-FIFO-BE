package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.CommentCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.CommentGetAllResponse;
import com.FA24SE088.OnlineForum.dto.request.CommentUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReplyCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.Comment;
import com.FA24SE088.OnlineForum.entity.Follow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    FollowResponse toRespone(Follow follow);
    Follow2Response toResponse2(Follow follow);
}
