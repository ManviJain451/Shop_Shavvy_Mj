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

    @Column(name = "gst", nullable = false)
    private String gst;

    @Column(name = "company_contact", nullable = false)
    private String companyContact;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @OneToMany(mappedBy = "seller")
    private Set<Product> products;
}