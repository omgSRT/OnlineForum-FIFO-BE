package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.Image;
import com.FA24SE088.OnlineForum.entity.Post;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-07T22:51:39+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class RedeemMapperImpl implements RedeemMapper {

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
        postResponse.imageList( imageListToImageResponseList( post.getImageList() ) );

        return postResponse.build();
    }

    protected ImageResponse imageToImageResponse(Image image) {
        if ( image == null ) {
            return null;
        }

        ImageResponse.ImageResponseBuilder imageResponse = ImageResponse.builder();

        imageResponse.imageId( image.getImageId() );
        imageResponse.url( image.getUrl() );

        return imageResponse.build();
    }

    protected List<ImageResponse> imageListToImageResponseList(List<Image> list) {
        if ( list == null ) {
            return null;
        }

        List<ImageResponse> list1 = new ArrayList<ImageResponse>( list.size() );
        for ( Image image : list ) {
            list1.add( imageToImageResponse( image ) );
        }

        return list1;
    }
}
