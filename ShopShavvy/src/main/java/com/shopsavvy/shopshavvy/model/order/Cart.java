package com.shopsavvy.shopshavvy.model.order;

import com.shopsavvy.shopshavvy.model.user.Customer;
import com.shopsavvy.shopshavvy.model.product.ProductVariation;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CartId.class)
public class Cart {
    @Id
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_variation_id")
    private ProductVariation productVariation;

    private int quantity;
    private boolean isWishlistItem;
}

