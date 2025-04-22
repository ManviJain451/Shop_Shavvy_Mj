package com.shopsavvy.shopshavvy.dto.categoryDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryMetadataFieldValueDTO {

    @NotBlank(message = "{validation.categoryId.mandatory")
    private String categoryId;

    @NotBlank(message = "{validation.metadata.fields.mandatory")
    private HashMap<String, String> metadataFieldWithValues;

}
