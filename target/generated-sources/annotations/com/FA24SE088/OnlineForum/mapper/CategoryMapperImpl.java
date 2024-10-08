package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.CategoryNoAccountRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-08T21:49:58+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toCategory(CategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( request.getName() );
        category.image( request.getImage() );

        return category.build();
    }

    @Override
    public Category toCategoryWithNoAccount(CategoryNoAccountRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( request.getName() );
        category.image( request.getImage() );

        return category.build();
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.categoryId( category.getCategoryId() );
        categoryResponse.name( category.getName() );
        categoryResponse.image( category.getImage() );
        categoryResponse.account( category.getAccount() );

        return categoryResponse.build();
    }

    @Override
    public CategoryNoAccountResponse toCategoryNoAccountResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryNoAccountResponse.CategoryNoAccountResponseBuilder categoryNoAccountResponse = CategoryNoAccountResponse.builder();

        categoryNoAccountResponse.categoryId( category.getCategoryId() );
        categoryNoAccountResponse.name( category.getName() );
        categoryNoAccountResponse.image( category.getImage() );

        return categoryNoAccountResponse.build();
    }

    @Override
    public void updateCategory(Category category, CategoryUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        category.setName( request.getName() );
        category.setImage( request.getImage() );
    }
}
