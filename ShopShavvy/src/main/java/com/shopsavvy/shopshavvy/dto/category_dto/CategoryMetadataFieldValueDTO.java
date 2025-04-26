package com.shopsavvy.shopshavvy.dto.category_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryMetadataFieldValueDTO {

    @NotBlank(message = "{validation.categoryId.mandatory}")
    private String categoryId;

    @NotNull(message = "{validation.metadata.fields.mandatory}")
    private HashMap<String, String> metadataFieldWithValues;

}
