package com.shopsavvy.shopshavvy.utilities;

import com.shopsavvy.shopshavvy.dto.productDto.ProductFilterDTO;

public class StringToDtoParser {

    public static ProductFilterDTO parseQueryToFilterDTO(String query) {
        ProductFilterDTO.ProductFilterDTOBuilder builder = ProductFilterDTO.builder();

        if (query != null && !query.isBlank()) {
            String[] pairs = query.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().toLowerCase();
                    String value = keyValue[1].trim();

                    switch (key) {
                        case "name": builder.name(value); break;
                        case "brand": builder.brand(value); break;
                        case "description": builder.description(value); break;
                        case "categoryid": builder.categoryId(value); break;
                        case "sellerid": builder.sellerId(value); break;
                        case "active" : builder.active(Boolean.parseBoolean(value)); break;
                    }
                }
            }
        }

        return builder.build();
    }

}
