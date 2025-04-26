package com.shopsavvy.shopshavvy.dto.product_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopsavvy.shopshavvy.dto.category_dto.CategoryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "{validation.product.name.mandatory}")
    @Size(max = 255, message = "{validation.productName.size}")
    private String productName;

    @NotBlank(message = "{validation.brand.mandatory}")
    @Size(max = 255, message = "{validation.brand.size}")
    private String brand;

    @NotBlank(message = "{validation.category.id.mandatory}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String categoryId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;

    @Size(max = 255, message = "{validation.description.size}")
    private String description;

    private boolean cancellable;
    private boolean returnable;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CategoryDTO categoryDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<ProductVariationResponseDTO> productVariations;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sellerId;

}
