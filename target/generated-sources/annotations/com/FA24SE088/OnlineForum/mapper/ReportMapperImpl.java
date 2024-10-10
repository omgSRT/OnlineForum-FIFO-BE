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
    date = "2024-10-10T14:30:31+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ReportMapperImpl implements ReportMapper {

    @Override
    public Report toReport(ReportRequest request) {
        if ( request == null ) {
            return null;
        }

        Report.ReportBuilder report = Report.builder();

        report.content( request.getContent() );

        return report.build();
    }

    @Override
    public ReportResponse toReportResponse(Report report) {
        if ( report == null ) {
            return null;
        }

        ReportResponse.ReportResponseBuilder reportResponse = ReportResponse.builder();

        reportResponse.postId( reportPostPostId( report ) );
        reportResponse.postTitle( reportPostTitle( report ) );
        reportResponse.postContent( reportPostContent( report ) );
        reportResponse.postCreatedDate( reportPostCreatedDate( report ) );
        reportResponse.postLastModifiedDate( reportPostLastModifiedDate( report ) );
        reportResponse.postStatus( reportPostStatus( report ) );
        reportResponse.content( report.getContent() );
        reportResponse.status( report.getStatus() );

        return reportResponse.build();
    }

    private UUID reportPostPostId(Report report) {
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

    private String reportPostTitle(Report report) {
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

    private String reportPostContent(Report report) {
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

    private Date reportPostCreatedDate(Report report) {
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

    private Date reportPostLastModifiedDate(Report report) {
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

    private String reportPostStatus(Report report) {
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
