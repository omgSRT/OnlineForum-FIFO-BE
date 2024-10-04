package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.entity.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PointMapper {
    Point toPoint(PointRequest request);

    PointResponse toPointResponse(Point point);

    @Mapping(target = "dailyPointList", ignore = true)
    void updatePoint(@MappingTarget Point point, PointRequest request);
}
