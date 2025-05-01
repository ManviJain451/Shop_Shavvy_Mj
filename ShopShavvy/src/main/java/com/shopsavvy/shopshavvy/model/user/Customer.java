package com.shopsavvy.shopshavvy.model.user;

import com.shopsavvy.shopshavvy.model.order.Cart;
import com.shopsavvy.shopshavvy.model.order.Order;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "customer_id")
public class Customer extends User {

    @Column(name = "contact", nullable = false)
    private String contact;

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders =  new HashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Cart> carts;
}


