package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.response.UpvoteCreateDeleteResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteNoPostResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteResponse;
import com.FA24SE088.OnlineForum.entity.Upvote;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-17T13:47:18+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class UpvoteMapperImpl implements UpvoteMapper {

    @Override
    public UpvoteResponse toUpvoteResponse(Upvote upvote) {
        if ( upvote == null ) {
            return null;
        }

        UpvoteResponse.UpvoteResponseBuilder upvoteResponse = UpvoteResponse.builder();

        upvoteResponse.upvoteId( upvote.getUpvoteId() );
        upvoteResponse.account( upvote.getAccount() );
        upvoteResponse.post( upvote.getPost() );

        return upvoteResponse.build();
    }

    @Override
    public UpvoteCreateDeleteResponse toUpvoteCreateDeleteResponse(Upvote upvote) {
        if ( upvote == null ) {
            return null;
        }

        UpvoteCreateDeleteResponse.UpvoteCreateDeleteResponseBuilder upvoteCreateDeleteResponse = UpvoteCreateDeleteResponse.builder();

        upvoteCreateDeleteResponse.upvoteId( upvote.getUpvoteId() );
        upvoteCreateDeleteResponse.account( upvote.getAccount() );
        upvoteCreateDeleteResponse.post( upvote.getPost() );

        return upvoteCreateDeleteResponse.build();
    }

    @Override
    public UpvoteNoPostResponse toUpvoteNoPostResponse(Upvote upvote) {
        if ( upvote == null ) {
            return null;
        }

        UpvoteNoPostResponse.UpvoteNoPostResponseBuilder upvoteNoPostResponse = UpvoteNoPostResponse.builder();

        upvoteNoPostResponse.upvoteId( upvote.getUpvoteId() );
        upvoteNoPostResponse.account( upvote.getAccount() );

        return upvoteNoPostResponse.build();
    }
}
