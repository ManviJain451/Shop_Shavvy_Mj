package com.shopsavvy.shopshavvy.dto.productDto;


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
    private String categoryId;
    private String sellerId;
    private boolean active;
}
