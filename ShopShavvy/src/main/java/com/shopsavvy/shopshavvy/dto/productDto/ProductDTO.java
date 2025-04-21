package com.shopsavvy.shopshavvy.dto.productDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDTO;
import com.shopsavvy.shopshavvy.validation.groups.Views;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Category ID is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String categoryId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;

    private String description;
    private boolean cancellable;
    private boolean returnable;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CategoryDTO categoryDetails;

    @JsonView(Views.CustomerView.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<ProductVariationDTO> productVariations;
}
