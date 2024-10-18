package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.entity.Redeem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-18T12:42:12+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class RedeemMapperImpl implements RedeemMapper {

    @Override
    public RedeemResponse toResponse(Redeem redeem) {
        if ( redeem == null ) {
            return null;
        }

        RedeemResponse.RedeemResponseBuilder redeemResponse = RedeemResponse.builder();

        redeemResponse.redeemId( redeem.getRedeemId() );
        redeemResponse.createdDate( redeem.getCreatedDate() );
        redeemResponse.account( redeem.getAccount() );
        redeemResponse.document( redeem.getDocument() );

        return redeemResponse.build();
    }
}
