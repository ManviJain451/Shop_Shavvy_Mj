package com.shopsavvy.shopshavvy.dto.categoryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CategoryDetailsDTO {

    private String id;
    private String name;
    private List<CategoryResponseDTO> parentCategories;
    private List<CategoryResponseDTO> immediateChildrens;
    private HashMap<String, String> metadataFieldsWithValues;
}
