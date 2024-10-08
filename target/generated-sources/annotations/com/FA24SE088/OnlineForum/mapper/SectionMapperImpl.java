package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import com.FA24SE088.OnlineForum.entity.Section;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
<<<<<<< HEAD
    date = "2024-10-08T22:02:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
=======
    date = "2024-10-07T23:12:19+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
>>>>>>> 38a6fd1acd56c138fc4dd03f0f9a012261b21bc4
)
@Component
public class SectionMapperImpl implements SectionMapper {

    @Override
    public Section toSection(SectionRequest request) {
        if ( request == null ) {
            return null;
        }

        Section.SectionBuilder section = Section.builder();

        section.linkGit( request.getLinkGit() );

        return section.build();
    }
}
