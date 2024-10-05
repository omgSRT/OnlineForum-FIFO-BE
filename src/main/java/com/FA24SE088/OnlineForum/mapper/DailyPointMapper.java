package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DailyPointRequest;
import com.FA24SE088.OnlineForum.dto.response.DailyPointResponse;
import com.FA24SE088.OnlineForum.entity.DailyPoint;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DailyPointMapper {
    DailyPoint toDailyPoint(DailyPointRequest request);

    DailyPointResponse toDailyPointResponse(DailyPoint dailyPoint);
}
