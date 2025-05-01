package com.shopsavvy.shopshavvy.dto.category;

import lombok.*;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDetailsForSellerDTO {

    private String id;
    private String name;
    private List<CategoryResponseDTO> parentCategories;
    private HashMap<String, String> metadataFieldsWithValues;

}
