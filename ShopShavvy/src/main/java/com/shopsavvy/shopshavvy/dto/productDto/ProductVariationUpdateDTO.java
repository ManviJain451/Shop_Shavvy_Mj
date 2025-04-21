package com.shopsavvy.shopshavvy.dto.productDto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariationUpdateDTO {
    private Integer quantity;
    private Double price;
    private Map<String, Object> metadata;
    private Boolean active;
}
