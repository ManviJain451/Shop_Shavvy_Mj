package com.shopsavvy.shopshavvy.dto.categoryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CategeoryDetailsDTO {

    private String id;
    private String name;
    private List<CategoryResponseDTO> parentCategories;
    private List<CategoryResponseDTO> immediateChildrens;
    private List<MetadataFieldDTO> metadataFields;

}
