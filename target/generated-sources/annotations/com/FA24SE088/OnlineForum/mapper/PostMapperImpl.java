package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-05T20:43:28+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class PostMapperImpl implements PostMapper {

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
}
