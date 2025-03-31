package com.shopsavvy.shopshavvy.model.products;

import com.shopsavvy.shopshavvy.model.users.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@IdClass(ProductReviewId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    @Id
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String review;
    private int rating;

    @CreatedDate
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;
}

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class ProductReviewId implements Serializable {
    private Customer customer;
    private Product product;
}