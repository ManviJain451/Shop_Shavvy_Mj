package com.shopsavvy.shopshavvy.dto.product;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdateDTO {

    @Size(max = 255, message = "{validation.productName.size}")
    private String name;

    @Size(max = 255, message = "{validation.max.description.size}")
    private String description;

    private Boolean cancellable;

    private Boolean returnable;
}
