package com.FA24SE088.OnlineForum.mapper;


import com.FA24SE088.OnlineForum.dto.request.MonkeyCoinPackRequest;
import com.FA24SE088.OnlineForum.dto.response.MonkeyCoinPackResponse;
import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonkeyCoinPackMapper {

    MonkeyCoinPack toMonkeyCoinPack(MonkeyCoinPackRequest request);

    @Mapping(target = "imgUrl", source = "imgUrl")
    MonkeyCoinPackResponse toResponse(MonkeyCoinPack monkeyCoinPack);
}
