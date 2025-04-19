package com.shopsavvy.shopshavvy.model.orders;

import com.shopsavvy.shopshavvy.model.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    private Long amountPaid;

    @CreatedDate
    private LocalDateTime dateCreated;

    private String paymentMethod;

    private String customerAddressCity;

    private String customerAddressState;

    private String customerAddressCountry;

    private String customerAddressLine;

    private String customerAddressZipCode;

    private String customerAddressLabel;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order_Product> orderProducts;
}