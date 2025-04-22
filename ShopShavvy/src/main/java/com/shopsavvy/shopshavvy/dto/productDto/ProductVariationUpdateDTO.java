package com.shopsavvy.shopshavvy.dto.productDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariationUpdateDTO {
    @NotNull(message = "{validation.price.mandatory}")
    @Min(value = 0, message = "{validation.price.value}")
    private Double price;

    @NotNull(message = "{validation.quantity.mandatory}")
    @Min(value = 0, message = "{validation.quantity.value}")
    private Integer quantity;

    @NotEmpty(message = "{validation.metadata.mandatory}")
    private Map<String, Object> metadata;

    private Boolean active;
}
