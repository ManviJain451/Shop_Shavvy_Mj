package com.shopsavvy.shopshavvy.model.users;

import com.shopsavvy.shopshavvy.model.products.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "sellers")
@PrimaryKeyJoinColumn(name = "seller_id")
public class Seller extends User {

    @Column(name = "gst", nullable = false, unique = true)
    private String gst;

    @Column(name = "company_contact", nullable = false, unique = true)
    private String companyContact;

    @Column(name = "company_name", nullable = false, unique = true)
    private String companyName;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product> products;
}


