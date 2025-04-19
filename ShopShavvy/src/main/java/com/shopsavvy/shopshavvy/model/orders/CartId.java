package com.shopsavvy.shopshavvy.model.orders;

import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import com.shopsavvy.shopshavvy.model.users.Customer;
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
