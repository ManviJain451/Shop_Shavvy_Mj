package com.shopsavvy.shopshavvy.dto.productDto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.shopsavvy.shopshavvy.validation.groups.Views;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariationDTO {

    @NotBlank(message = "{validation.product.id.mandatory}")
    private String productId;

    @NotNull(message = "{validation.price.mandatory}")
    @Min(value = 0, message = "Price must be 0 or more")
    private Double price;

    @NotNull(message = "{validation.quantity.mandatory}")
    @Min(value = 0, message = "{validation.quantity.value}")
    private Integer quantity;

    @NotEmpty(message = "{validation.metadata.mandatory}")
    private Map<String, Object> metadata;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ProductDTO parentProduct;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonView(Views.AdminView.class)
    private String primaryImage;

}
