package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.response.BookMarkResponse;
import com.FA24SE088.OnlineForum.entity.BookMark;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface BookMarkMapper {
    BookMarkResponse toResponse(BookMark bookMark);

}
