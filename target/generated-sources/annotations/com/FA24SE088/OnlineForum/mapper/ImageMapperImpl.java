package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.ImageRequest;
import com.FA24SE088.OnlineForum.entity.Image;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-07T16:46:18+0700",
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
}
