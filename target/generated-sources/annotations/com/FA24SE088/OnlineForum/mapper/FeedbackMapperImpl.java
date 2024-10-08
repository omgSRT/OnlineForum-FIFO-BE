package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.Feedback;
import com.FA24SE088.OnlineForum.entity.Post;
import java.util.Date;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-08T17:07:21+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class FeedbackMapperImpl implements FeedbackMapper {

    @Override
    public Feedback toFeedback(FeedbackRequest request) {
        if ( request == null ) {
            return null;
        }

        Feedback.FeedbackBuilder feedback = Feedback.builder();

        feedback.title( request.getTitle() );
        feedback.content( request.getContent() );

        return feedback.build();
    }

    @Override
    public FeedbackResponse toFeedbackResponse(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }

        FeedbackResponse.FeedbackResponseBuilder feedbackResponse = FeedbackResponse.builder();

        feedbackResponse.postId( feedbackPostPostId( feedback ) );
        feedbackResponse.postTitle( feedbackPostTitle( feedback ) );
        feedbackResponse.postContent( feedbackPostContent( feedback ) );
        feedbackResponse.postCreatedDate( feedbackPostCreatedDate( feedback ) );
        feedbackResponse.postLastModifiedDate( feedbackPostLastModifiedDate( feedback ) );
        feedbackResponse.postStatus( feedbackPostStatus( feedback ) );
        feedbackResponse.feedbackId( feedback.getFeedbackId() );
        feedbackResponse.title( feedback.getTitle() );
        feedbackResponse.content( feedback.getContent() );
        feedbackResponse.status( feedback.getStatus() );

        return feedbackResponse.build();
    }

    private UUID feedbackPostPostId(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Post post = feedback.getPost();
        if ( post == null ) {
            return null;
        }
        UUID postId = post.getPostId();
        if ( postId == null ) {
            return null;
        }
        return postId;
    }

    private String feedbackPostTitle(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Post post = feedback.getPost();
        if ( post == null ) {
            return null;
        }
        String title = post.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }

    private String feedbackPostContent(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Post post = feedback.getPost();
        if ( post == null ) {
            return null;
        }
        String content = post.getContent();
        if ( content == null ) {
            return null;
        }
        return content;
    }

    private Date feedbackPostCreatedDate(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Post post = feedback.getPost();
        if ( post == null ) {
            return null;
        }
        Date createdDate = post.getCreatedDate();
        if ( createdDate == null ) {
            return null;
        }
        return createdDate;
    }

    private Date feedbackPostLastModifiedDate(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Post post = feedback.getPost();
        if ( post == null ) {
            return null;
        }
        Date lastModifiedDate = post.getLastModifiedDate();
        if ( lastModifiedDate == null ) {
            return null;
        }
        return lastModifiedDate;
    }

    private String feedbackPostStatus(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Post post = feedback.getPost();
        if ( post == null ) {
            return null;
        }
        String status = post.getStatus();
        if ( status == null ) {
            return null;
        }
        return status;
    }
}
