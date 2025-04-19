package com.shopsavvy.shopshavvy.model.products;

import com.shopsavvy.shopshavvy.model.users.Customer;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductReviewId implements Serializable {
    private Customer customer;
    private Product product;
}
