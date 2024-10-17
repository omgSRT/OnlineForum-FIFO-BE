package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.response.FollowResponse;
import com.FA24SE088.OnlineForum.entity.Follow;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-17T23:13:10+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class FollowMapperImpl implements FollowMapper {

    @Override
    public FollowResponse toRespone(Follow follow) {
        if ( follow == null ) {
            return null;
        }

        FollowResponse.FollowResponseBuilder followResponse = FollowResponse.builder();

        followResponse.followId( follow.getFollowId() );
        followResponse.status( follow.getStatus() );
        followResponse.followee( follow.getFollowee() );
        followResponse.follower( follow.getFollower() );

        return followResponse.build();
    }
}
