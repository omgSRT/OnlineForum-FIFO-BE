package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Report;
import java.util.Date;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-08T22:26:56+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class ReportMapperImpl implements ReportMapper {

    @Override
    public Report toFeedback(ReportRequest request) {
        if ( request == null ) {
            return null;
        }

        Report.ReportBuilder report = Report.builder();

        report.content( request.getContent() );

        return report.build();
    }

    @Override
    public ReportResponse toFeedbackResponse(Report feedback) {
        if ( feedback == null ) {
            return null;
        }

        ReportResponse.ReportResponseBuilder reportResponse = ReportResponse.builder();

        reportResponse.postId( feedbackPostPostId( feedback ) );
        reportResponse.postTitle( feedbackPostTitle( feedback ) );
        reportResponse.postContent( feedbackPostContent( feedback ) );
        reportResponse.postCreatedDate( feedbackPostCreatedDate( feedback ) );
        reportResponse.postLastModifiedDate( feedbackPostLastModifiedDate( feedback ) );
        reportResponse.postStatus( feedbackPostStatus( feedback ) );
        reportResponse.content( feedback.getContent() );
        reportResponse.status( feedback.getStatus() );

        return reportResponse.build();
    }

    private UUID feedbackPostPostId(Report report) {
        if ( report == null ) {
            return null;
        }
        Post post = report.getPost();
        if ( post == null ) {
            return null;
        }
        UUID postId = post.getPostId();
        if ( postId == null ) {
            return null;
        }
        return postId;
    }

    private String feedbackPostTitle(Report report) {
        if ( report == null ) {
            return null;
        }
        Post post = report.getPost();
        if ( post == null ) {
            return null;
        }
        String title = post.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }

    private String feedbackPostContent(Report report) {
        if ( report == null ) {
            return null;
        }
        Post post = report.getPost();
        if ( post == null ) {
            return null;
        }
        String content = post.getContent();
        if ( content == null ) {
            return null;
        }
        return content;
    }

    private Date feedbackPostCreatedDate(Report report) {
        if ( report == null ) {
            return null;
        }
        Post post = report.getPost();
        if ( post == null ) {
            return null;
        }
        Date createdDate = post.getCreatedDate();
        if ( createdDate == null ) {
            return null;
        }
        return createdDate;
    }

    private Date feedbackPostLastModifiedDate(Report report) {
        if ( report == null ) {
            return null;
        }
        Post post = report.getPost();
        if ( post == null ) {
            return null;
        }
        Date lastModifiedDate = post.getLastModifiedDate();
        if ( lastModifiedDate == null ) {
            return null;
        }
        return lastModifiedDate;
    }

    private String feedbackPostStatus(Report report) {
        if ( report == null ) {
            return null;
        }
        Post post = report.getPost();
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
