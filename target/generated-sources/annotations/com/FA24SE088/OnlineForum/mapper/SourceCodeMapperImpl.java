package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.entity.Document;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-07T22:51:39+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class SourceCodeMapperImpl implements SourceCodeMapper {

    @Override
    public Document toSourceCode(DocumentRequest request) {
        if ( request == null ) {
            return null;
        }

        Document.DocumentBuilder document = Document.builder();

        document.name( request.getName() );
        document.image( request.getImage() );
        document.price( request.getPrice() );
        document.type( request.getType() );
        document.status( request.getStatus() );

        return document.build();
    }

    @Override
    public DocumentResponse toResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentResponse.DocumentResponseBuilder documentResponse = DocumentResponse.builder();

        documentResponse.name( document.getName() );
        documentResponse.image( document.getImage() );
        documentResponse.price( document.getPrice() );
        documentResponse.type( document.getType() );
        documentResponse.status( document.getStatus() );

        return documentResponse.build();
    }
}
