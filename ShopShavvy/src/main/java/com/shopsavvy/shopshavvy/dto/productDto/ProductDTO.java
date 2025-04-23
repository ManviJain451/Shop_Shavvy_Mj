package com.shopsavvy.shopshavvy.dto.productDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDTO;
import com.shopsavvy.shopshavvy.model.users.User;
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

    @NotBlank(message = "{validation.product.name.mandatory}")
    private String productName;

    @NotBlank(message = "{validation.brand.mandatory}")
    private String brand;

    @NotBlank(message = "{validation.category.id.mandatory}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String categoryId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;

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
