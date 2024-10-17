package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.ReportAccountRequest;
import com.FA24SE088.OnlineForum.dto.response.ReportAccountResponse;
import com.FA24SE088.OnlineForum.entity.ReportAccount;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-17T13:47:18+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ReportAccountMapperImpl implements ReportAccountMapper {

    @Override
    public ReportAccount toReportAccount(ReportAccountRequest request) {
        if ( request == null ) {
            return null;
        }

        ReportAccount.ReportAccountBuilder reportAccount = ReportAccount.builder();

        reportAccount.reason( request.getReason() );
        reportAccount.status( request.getStatus() );

        return reportAccount.build();
    }

    @Override
    public ReportAccountResponse toResponse(ReportAccount account) {
        if ( account == null ) {
            return null;
        }

        ReportAccountResponse.ReportAccountResponseBuilder reportAccountResponse = ReportAccountResponse.builder();

        reportAccountResponse.reportAccountId( account.getReportAccountId() );
        reportAccountResponse.reason( account.getReason() );
        reportAccountResponse.reportTime( account.getReportTime() );
        reportAccountResponse.status( account.getStatus() );

        return reportAccountResponse.build();
    }
}
