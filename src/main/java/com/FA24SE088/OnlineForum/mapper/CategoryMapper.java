package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.CategoryNoAccountRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest request);

    Category toCategoryWithNoAccount(CategoryNoAccountRequest request);

    CategoryResponse toCategoryResponse(Category category);

    CategoryNoAccountResponse toCategoryNoAccountResponse(Category category);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "topicList", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryUpdateRequest request);
}
