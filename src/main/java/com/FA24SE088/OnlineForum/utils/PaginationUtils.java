package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PaginationUtils {
    public <T> List<T> convertListToPage(int page, int perPage, List<T> list){
        if(list.isEmpty()){
            throw new AppException(ErrorCode.EMPTY_LIST);
        }
        if(page <= 0){
            throw new AppException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if(perPage <= 0){
            throw new AppException(ErrorCode.INVALID_PER_PAGE_NUMBER);
        }

        //turn arraylist to page
        PageRequest pageable = PageRequest.of(page - 1, perPage);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        log.info(SuccessReturnMessage.CONVERT_SUCCESS.getMessage());
        Page<T> paginationList = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return paginationList.getContent();
    }
}
