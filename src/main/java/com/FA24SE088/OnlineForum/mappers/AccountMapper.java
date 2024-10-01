package com.FA24SE088.OnlineForum.mappers;

import com.FA24SE088.OnlineForum.dto.requests.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(AccountRequest request);
    AccountResponse toResponse(Account account);
}
