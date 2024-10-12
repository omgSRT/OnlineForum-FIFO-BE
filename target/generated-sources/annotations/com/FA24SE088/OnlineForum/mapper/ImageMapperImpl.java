package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.ImageCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.ImageRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageResponse;
import com.FA24SE088.OnlineForum.entity.Image;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-12T12:47:03+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ImageMapperImpl implements ImageMapper {

    @Override
    public Image toImage(ImageRequest request) {
        if ( request == null ) {
            return null;
        }

        Image.ImageBuilder image = Image.builder();

        image.url( request.getUrl() );

        return image.build();
    }

    @Override
    public Image toImage(ImageCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Image.ImageBuilder image = Image.builder();

        return image.build();
    }

    @Override
    public ImageResponse toImageResponse(Image image) {
        if ( image == null ) {
            return null;
        }

        ImageResponse.ImageResponseBuilder imageResponse = ImageResponse.builder();

        imageResponse.imageId( image.getImageId() );
        imageResponse.url( image.getUrl() );

        return imageResponse.build();
    }
}
