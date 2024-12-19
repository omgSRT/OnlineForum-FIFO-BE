package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DailyPointRequest;
import com.FA24SE088.OnlineForum.dto.response.DailyPointForFilterTransactionResponse;
import com.FA24SE088.OnlineForum.dto.response.DailyPointResponse;
import com.FA24SE088.OnlineForum.entity.DailyPoint;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DailyPointMapper {
    DailyPoint toDailyPoint(DailyPointRequest request);

    DailyPoint toDailyPoint(DailyPoint dailyPoint);

    DailyPointResponse toDailyPointResponse(DailyPoint dailyPoint);

    DailyPointForFilterTransactionResponse toDailyPointResponse2(DailyPoint dailyPoint);

    List<DailyPointForFilterTransactionResponse> toListResponse(List<DailyPoint> list);
}
