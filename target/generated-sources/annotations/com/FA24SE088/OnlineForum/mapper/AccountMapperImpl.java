package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-17T22:48:18+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
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
        account.email( request.getEmail() );
        account.password( request.getPassword() );
        account.avatar( request.getAvatar() );
        account.coverImage( request.getCoverImage() );

        return account.build();
    }

    @Override
    public AccountResponse toResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponse.AccountResponseBuilder accountResponse = AccountResponse.builder();

        accountResponse.accountId( account.getAccountId() );
        accountResponse.username( account.getUsername() );
        accountResponse.email( account.getEmail() );
        accountResponse.avatar( account.getAvatar() );
        accountResponse.createdDate( account.getCreatedDate() );
        accountResponse.status( account.getStatus() );
        accountResponse.role( account.getRole() );
        List<Category> list = account.getCategoryList();
        if ( list != null ) {
            accountResponse.categoryList( new ArrayList<Category>( list ) );
        }
        accountResponse.wallet( account.getWallet() );

        return accountResponse.build();
    }

    @Override
    public void updateAccount(Account account, AccountUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        account.setEmail( request.getEmail() );
        account.setAvatar( request.getAvatar() );
    }
}
