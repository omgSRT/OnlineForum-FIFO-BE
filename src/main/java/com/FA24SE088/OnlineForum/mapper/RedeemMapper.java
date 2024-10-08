package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Redeem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RedeemMapper {

    RedeemResponse toResponse(Redeem redeem);
}
