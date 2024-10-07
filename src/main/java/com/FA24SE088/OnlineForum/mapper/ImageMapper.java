package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.ImageCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.ImageRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageResponse;
import com.FA24SE088.OnlineForum.entity.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    Image toImage(ImageRequest request);

    Image toImage(ImageCreateRequest request);

    ImageResponse toImageResponse(Image image);
}
