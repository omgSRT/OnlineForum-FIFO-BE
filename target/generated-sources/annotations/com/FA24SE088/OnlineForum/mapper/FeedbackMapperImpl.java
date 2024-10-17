package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Feedback;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-17T22:48:18+0700",
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

        feedback.content( request.getContent() );
        feedback.title( request.getTitle() );

        return feedback.build();
    }

    @Override
    public FeedbackResponse toResponse(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }

        FeedbackResponse.FeedbackResponseBuilder feedbackResponse = FeedbackResponse.builder();

        feedbackResponse.feedbackId( feedback.getFeedbackId() );
        feedbackResponse.accountId( feedbackAccountAccountId( feedback ) );
        feedbackResponse.title( feedback.getTitle() );
        feedbackResponse.content( feedback.getContent() );
        feedbackResponse.status( feedback.getStatus() );

        return feedbackResponse.build();
    }

    private UUID feedbackAccountAccountId(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        Account account = feedback.getAccount();
        if ( account == null ) {
            return null;
        }
        UUID accountId = account.getAccountId();
        if ( accountId == null ) {
            return null;
        }
        return accountId;
    }
}
