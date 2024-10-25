package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "categoryList", ignore = true)
    Account toAccount(AccountRequest request);

    AccountResponse toResponse(Account account);

    void updateAccount(@MappingTarget Account account, AccountUpdateRequest request);
}
