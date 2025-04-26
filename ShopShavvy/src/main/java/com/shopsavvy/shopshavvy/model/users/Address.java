package com.shopsavvy.shopshavvy.model.users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "label")
    private String label;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

}