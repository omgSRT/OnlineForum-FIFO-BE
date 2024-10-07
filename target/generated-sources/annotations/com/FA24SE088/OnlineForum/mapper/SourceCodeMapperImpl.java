package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.SourceCodeRequest;
import com.FA24SE088.OnlineForum.dto.response.SourceCodeResponse;
import com.FA24SE088.OnlineForum.entity.SourceCode;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-07T16:46:18+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class SourceCodeMapperImpl implements SourceCodeMapper {

    @Override
    public SourceCode toSourceCode(SourceCodeRequest request) {
        if ( request == null ) {
            return null;
        }

        SourceCode.SourceCodeBuilder sourceCode = SourceCode.builder();

        sourceCode.name( request.getName() );
        sourceCode.image( request.getImage() );
        sourceCode.price( request.getPrice() );
        sourceCode.type( request.getType() );
        sourceCode.status( request.getStatus() );

        return sourceCode.build();
    }

    @Override
    public SourceCodeResponse toResponse(SourceCode sourceCode) {
        if ( sourceCode == null ) {
            return null;
        }

        SourceCodeResponse.SourceCodeResponseBuilder sourceCodeResponse = SourceCodeResponse.builder();

        sourceCodeResponse.name( sourceCode.getName() );
        sourceCodeResponse.image( sourceCode.getImage() );
        sourceCodeResponse.price( sourceCode.getPrice() );
        sourceCodeResponse.type( sourceCode.getType() );
        sourceCodeResponse.status( sourceCode.getStatus() );

        return sourceCodeResponse.build();
    }
}
