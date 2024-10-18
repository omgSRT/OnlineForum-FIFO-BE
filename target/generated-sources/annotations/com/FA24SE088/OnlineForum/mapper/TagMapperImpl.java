package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.entity.Tag;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-18T12:42:12+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class TagMapperImpl implements TagMapper {

    @Override
    public Tag toTag(TagRequest request) {
        if ( request == null ) {
            return null;
        }

        Tag.TagBuilder tag = Tag.builder();

        tag.name( request.getName() );
        tag.colorHex( request.getColorHex() );

        return tag.build();
    }

    @Override
    public TagResponse toTagResponse(Tag tag) {
        if ( tag == null ) {
            return null;
        }

        TagResponse.TagResponseBuilder tagResponse = TagResponse.builder();

        tagResponse.tagId( tag.getTagId() );
        tagResponse.name( tag.getName() );
        tagResponse.colorHex( tag.getColorHex() );

        return tagResponse.build();
    }

    @Override
    public void updateTag(Tag tag, TagRequest request) {
        if ( request == null ) {
            return;
        }

        tag.setName( request.getName() );
        tag.setColorHex( request.getColorHex() );
    }
}
