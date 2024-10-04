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
    date = "2024-10-04T15:46:10+0700",
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
        accountResponse.bio( account.getBio() );
        accountResponse.gender( account.getGender() );
        accountResponse.address( account.getAddress() );
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

        account.setHandle( request.getHandle() );
        account.setEmail( request.getEmail() );
        account.setBio( request.getBio() );
        account.setGender( request.getGender() );
        account.setAddress( request.getAddress() );
        account.setAvatar( request.getAvatar() );
    }
}
