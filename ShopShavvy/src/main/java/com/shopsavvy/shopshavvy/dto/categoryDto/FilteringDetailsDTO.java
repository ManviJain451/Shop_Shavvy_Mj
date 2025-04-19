package com.shopsavvy.shopshavvy.dto.categoryDto;

import lombok.*;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilteringDetailsDTO {
    private String id;
    private String categoryName;
    private HashMap<String, String> metadataFields;
    private List<String> brands;
    private Double minPrice;
    private Double maxPrice;
}