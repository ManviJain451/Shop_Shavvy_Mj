package com.shopsavvy.shopshavvy.dto.productDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdateDTO {
    private String name;
    private String description;
    private Boolean cancellable;
    private Boolean returnable;
}
