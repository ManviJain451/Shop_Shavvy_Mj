package com.shopsavvy.shopshavvy.dto.categoryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CategoryResponseDTO {

    String categoryId;
    String categoryName;
    String parentCategoryId;
}

