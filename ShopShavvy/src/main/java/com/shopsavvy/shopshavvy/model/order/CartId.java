package com.shopsavvy.shopshavvy.model.order;

import com.shopsavvy.shopshavvy.model.product.ProductVariation;
import com.shopsavvy.shopshavvy.model.user.Customer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class CartId implements Serializable {
    private Customer customer;
    private ProductVariation productVariation;
}
