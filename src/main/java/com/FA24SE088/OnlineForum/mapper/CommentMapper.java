package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.CommentCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.CommentGetAllResponse;
import com.FA24SE088.OnlineForum.dto.request.CommentUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReplyCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.CommentNoPostResponse;
import com.FA24SE088.OnlineForum.dto.response.CommentResponse;
import com.FA24SE088.OnlineForum.dto.response.ReplyCreateResponse;
import com.FA24SE088.OnlineForum.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentCreateRequest request);

    Comment toCommentFromReplyRequest(ReplyCreateRequest request);

    CommentResponse toCommentResponse(Comment comment);

    ReplyCreateResponse toReplyCreateResponse(Comment reply);

    CommentNoPostResponse toCommentNoPostResponse(Comment comment);
    default List<CommentNoPostResponse> toCommentNoPostResponseList(List<Comment> comments) {
        return comments.stream()
                .map(this::toCommentNoPostResponse)
                .toList();
    }
    default CommentNoPostResponse toCommentNoPostResponseWithReplies(Comment comment) {
        CommentNoPostResponse response = new CommentNoPostResponse();
        response.setCommentId(comment.getCommentId());
        response.setContent(comment.getContent());
        response.setAccount(comment.getAccount());

        if (comment.getReplies() != null) {
            response.setReplies(toCommentNoPostResponseList(comment.getReplies()));
        } else {
            response.setReplies(new ArrayList<>());
        }
        return response;
    }

    CommentGetAllResponse toCommentGetAllResponse(Comment comment);

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    void updateComment(@MappingTarget Comment comment, CommentUpdateRequest request);
}
