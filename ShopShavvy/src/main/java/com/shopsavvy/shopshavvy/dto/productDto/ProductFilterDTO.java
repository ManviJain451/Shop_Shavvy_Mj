package com.shopsavvy.shopshavvy.dto.productDto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilterDTO {
    private String name;
    private String brand;
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryId;

    private String sellerId;
    private boolean active;
}
