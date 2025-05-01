package com.shopsavvy.shopshavvy.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

