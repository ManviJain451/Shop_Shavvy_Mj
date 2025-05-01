package com.shopsavvy.shopshavvy.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariationUpdateDTO {

    @Min(value = 0, message = "{validation.price.value}")
    private Double price;

    private Integer quantity;

    @NotEmpty(message = "{validation.metadata.mandatory}")
    private Map<String, Object> metadata;

    private Boolean active;
}
