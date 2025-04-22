package com.shopsavvy.shopshavvy.dto.productDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariationResponseDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String productVariationId;

    private Double price;

    private Integer quantity;

    private Map<String, Object> metadata;

    private String primaryImage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> secondaryImages;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ProductResponseDTO parentProduct;

}
