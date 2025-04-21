package com.shopsavvy.shopshavvy.dto.productDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsavvy.shopshavvy.validation.groups.Views;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariationDTO {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be 0 or more")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantity;

    @NotEmpty(message = "Metadata fields are required")
    private Map<String, Object> metadata;

    @JsonView(Views.SellerView.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ProductDTO parentProduct;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonView(Views.AdminView.class)
    private String primaryImage;

}
