package com.shopsavvy.shopshavvy.dto.category_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {

    private String parentCategoryId;

    @NotBlank(message = "{validation.category.name.mandatory}")
    @Size(min = 2, max = 255, message = "{validation.category.name.max.size}")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "{validation.category.name.pattern}")
    private String categoryName;
}