package com.shopsavvy.shopshavvy.dto.productDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {
    private String productId;
    private String productName;
    private String brand;
    private String description;
    private boolean cancellable;
    private boolean returnable;
}
