package com.shopsavvy.shopshavvy.model.users;

import com.shopsavvy.shopshavvy.model.products.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sellers")
@PrimaryKeyJoinColumn(name = "seller_id")
public class Seller extends User {

    private String gst;

    private String companyContact;

    private String companyName;

    private Set<Product> products;
}