package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageResponse;
import com.FA24SE088.OnlineForum.dto.response.PostGetByIdResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.Comment;
import com.FA24SE088.OnlineForum.entity.Image;
import com.FA24SE088.OnlineForum.entity.Post;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-12T12:47:02+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Autowired
    private ImageMapper imageMapper;

    @Override
    public Post toPost(PostCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Post.PostBuilder post = Post.builder();

        post.title( request.getTitle() );
        post.content( request.getContent() );

        return post.build();
    }

    @Override
    public PostResponse toPostResponse(Post post) {
        if ( post == null ) {
            return null;
        }

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        postResponse.imageList( imageListToImageResponseList( post.getImageList() ) );
        postResponse.postId( post.getPostId() );
        postResponse.title( post.getTitle() );
        postResponse.content( post.getContent() );
        postResponse.createdDate( post.getCreatedDate() );
        postResponse.lastModifiedDate( post.getLastModifiedDate() );
        postResponse.status( post.getStatus() );
        postResponse.account( post.getAccount() );
        postResponse.topic( post.getTopic() );

        return postResponse.build();
    }

    @Override
    public PostGetByIdResponse toPostGetByIdResponse(Post post) {
        if ( post == null ) {
            return null;
        }

        PostGetByIdResponse.PostGetByIdResponseBuilder postGetByIdResponse = PostGetByIdResponse.builder();

        postGetByIdResponse.postId( post.getPostId() );
        postGetByIdResponse.title( post.getTitle() );
        postGetByIdResponse.content( post.getContent() );
        postGetByIdResponse.createdDate( post.getCreatedDate() );
        postGetByIdResponse.lastModifiedDate( post.getLastModifiedDate() );
        postGetByIdResponse.status( post.getStatus() );
        postGetByIdResponse.account( post.getAccount() );
        postGetByIdResponse.topic( post.getTopic() );
        List<Comment> list = post.getCommentList();
        if ( list != null ) {
            postGetByIdResponse.commentList( new ArrayList<Comment>( list ) );
        }

        return postGetByIdResponse.build();
    }

    @Override
    public void updatePost(Post post, PostUpdateRequest postUpdateRequest) {
        if ( postUpdateRequest == null ) {
            return;
        }

        post.setTitle( postUpdateRequest.getTitle() );
        post.setContent( postUpdateRequest.getContent() );
    }

    protected List<ImageResponse> imageListToImageResponseList(List<Image> list) {
        if ( list == null ) {
            return null;
        }

        List<ImageResponse> list1 = new ArrayList<ImageResponse>( list.size() );
        for ( Image image : list ) {
            list1.add( imageMapper.toImageResponse( image ) );
        }

        return list1;
    }
}
