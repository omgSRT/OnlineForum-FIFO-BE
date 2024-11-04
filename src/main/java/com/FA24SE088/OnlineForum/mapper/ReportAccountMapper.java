package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReportAccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportAccount2Response;
import com.FA24SE088.OnlineForum.dto.response.ReportAccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.ReportAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReportAccountMapper {
    ReportAccount toReportAccount(ReportAccountRequest request);

    @Mapping(target = "title", source = "title")
    ReportAccountResponse toResponse(ReportAccount account);

    @Mapping(target = "title", source = "title")
    ReportAccount2Response toResponse2(ReportAccount account);
}
