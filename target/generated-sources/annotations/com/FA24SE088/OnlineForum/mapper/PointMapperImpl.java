package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.entity.Point;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-07T22:51:39+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class PointMapperImpl implements PointMapper {

    @Override
    public Point toPoint(PointRequest request) {
        if ( request == null ) {
            return null;
        }

        Point.PointBuilder point = Point.builder();

        point.maxPoint( request.getMaxPoint() );
        point.pointPerPost( request.getPointPerPost() );

        return point.build();
    }

    @Override
    public PointResponse toPointResponse(Point point) {
        if ( point == null ) {
            return null;
        }

        PointResponse.PointResponseBuilder pointResponse = PointResponse.builder();

        pointResponse.pointId( point.getPointId() );
        pointResponse.maxPoint( point.getMaxPoint() );
        pointResponse.pointPerPost( point.getPointPerPost() );

        return pointResponse.build();
    }

    @Override
    public void updatePoint(Point point, PointRequest request) {
        if ( request == null ) {
            return;
        }

        point.setMaxPoint( request.getMaxPoint() );
        point.setPointPerPost( request.getPointPerPost() );
    }
}
