package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DailyPointRequest;
import com.FA24SE088.OnlineForum.dto.response.DailyPointResponse;
import com.FA24SE088.OnlineForum.entity.DailyPoint;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-05T20:43:28+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class DailyPointMapperImpl implements DailyPointMapper {

    @Override
    public DailyPoint toDailyPoint(DailyPointRequest request) {
        if ( request == null ) {
            return null;
        }

        DailyPoint.DailyPointBuilder dailyPoint = DailyPoint.builder();

        return dailyPoint.build();
    }

    @Override
    public DailyPointResponse toDailyPointResponse(DailyPoint dailyPoint) {
        if ( dailyPoint == null ) {
            return null;
        }

        DailyPointResponse.DailyPointResponseBuilder dailyPointResponse = DailyPointResponse.builder();

        dailyPointResponse.dailyPointId( dailyPoint.getDailyPointId() );
        dailyPointResponse.pointEarned( dailyPoint.getPointEarned() );
        dailyPointResponse.createdDate( dailyPoint.getCreatedDate() );
        dailyPointResponse.account( dailyPoint.getAccount() );
        dailyPointResponse.post( dailyPoint.getPost() );

        return dailyPointResponse.build();
    }
}
