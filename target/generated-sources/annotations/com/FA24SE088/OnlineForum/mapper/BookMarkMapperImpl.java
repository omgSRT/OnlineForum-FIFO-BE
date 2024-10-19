package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.response.BookMarkResponse;
import com.FA24SE088.OnlineForum.entity.BookMark;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-19T09:24:03+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class BookMarkMapperImpl implements BookMarkMapper {

    @Override
    public BookMarkResponse toResponse(BookMark bookMark) {
        if ( bookMark == null ) {
            return null;
        }

        BookMarkResponse.BookMarkResponseBuilder bookMarkResponse = BookMarkResponse.builder();

        bookMarkResponse.bookmarkID( bookMark.getBookmarkID() );
        bookMarkResponse.account( bookMark.getAccount() );
        bookMarkResponse.post( bookMark.getPost() );

        return bookMarkResponse.build();
    }
}
