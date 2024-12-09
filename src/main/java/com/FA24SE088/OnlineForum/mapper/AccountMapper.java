package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountFollowResponse;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "categoryList", ignore = true)
    Account toAccount(AccountRequest request);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "bio", source = "bio")
    AccountResponse toResponse(Account account);

    void updateAccount(@MappingTarget Account account, AccountUpdateRequest request);

    List<AccountResponse> toListResponse(List<Account> list);

    AccountFollowResponse toCountFollower(Account account);

    List<AccountFollowResponse> toCountFollowerList(List<Account> list);
}
