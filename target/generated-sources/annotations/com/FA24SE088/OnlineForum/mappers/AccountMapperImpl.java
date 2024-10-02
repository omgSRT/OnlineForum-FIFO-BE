package com.FA24SE088.OnlineForum.mappers;

import com.FA24SE088.OnlineForum.dto.requests.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-01T17:29:45+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public Account toAccount(AccountRequest request) {
        if ( request == null ) {
            return null;
        }

        Account.AccountBuilder account = Account.builder();

        account.username( request.getUsername() );
        account.handle( request.getHandle() );
        account.email( request.getEmail() );
        account.password( request.getPassword() );
        account.bio( request.getBio() );
        account.gender( request.getGender() );
        account.address( request.getAddress() );
        account.avatar( request.getAvatar() );

        return account.build();
    }

    @Override
    public AccountResponse toResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponse.AccountResponseBuilder accountResponse = AccountResponse.builder();

        accountResponse.username( account.getUsername() );
        accountResponse.handle( account.getHandle() );
        accountResponse.email( account.getEmail() );
        accountResponse.password( account.getPassword() );
        accountResponse.bio( account.getBio() );
        accountResponse.gender( account.getGender() );
        accountResponse.address( account.getAddress() );
        accountResponse.avatar( account.getAvatar() );
        accountResponse.createdDate( account.getCreatedDate() );
        accountResponse.status( account.getStatus() );
        accountResponse.role( account.getRole() );

        return accountResponse.build();
    }
}
