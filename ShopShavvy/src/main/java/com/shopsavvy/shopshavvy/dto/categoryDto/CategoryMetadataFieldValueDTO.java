package com.shopsavvy.shopshavvy.dto.categoryDto;

import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryMetadataFieldValueDTO {
    private String categoryId;
    private HashMap<String, String> metadataFieldWithValues;

}
