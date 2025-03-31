package com.shopsavvy.shopshavvy.model.users;

import com.shopsavvy.shopshavvy.model.orders.Cart;
import com.shopsavvy.shopshavvy.model.orders.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "customer_id")
public class Customer extends User {

    private String contact;

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders =  new HashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Cart> carts;
}


