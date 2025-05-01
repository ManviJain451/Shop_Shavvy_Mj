package com.shopsavvy.shopshavvy.model.order;

import com.shopsavvy.shopshavvy.model.product.ProductVariation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order_Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer quantity;

    private Long price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;

    @OneToMany
    @JoinColumn(name = "order_product_id")
    private Set<OrderStatus>  orderStatus = new HashSet<>();

}
