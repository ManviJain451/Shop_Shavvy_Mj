package com.shopsavvy.shopshavvy.model.product;

import com.shopsavvy.shopshavvy.model.user.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@IdClass(ProductReviewId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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

