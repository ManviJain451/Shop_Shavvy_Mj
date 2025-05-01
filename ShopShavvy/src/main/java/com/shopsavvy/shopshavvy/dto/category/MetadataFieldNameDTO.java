package com.shopsavvy.shopshavvy.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataFieldNameDTO {

    @NotBlank(message = "{validation.fieldName.mandatory}")
    @Size(min = 2, max = 255, message = "{validation.fieldName.max.size}")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "{validation.field.name.pattern}")
    private String fieldName;

}
