package com.shopsavvy.shopshavvy.model.orders;

import com.shopsavvy.shopshavvy.model.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    @Column(name = "amount_paid", nullable = false)
    private Long amountPaid;

    @CreatedDate
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "customer_address_city", nullable = false)
    private String customerAddressCity;

    @Column(name = "customer_address_state", nullable = false)
    private String customerAddressState;

    @Column(name = "customer_address_country", nullable = false)
    private String customerAddressCountry;

    @Column(name = "customer_address_address_line", nullable = false)
    private String customerAddressAddressLine;

    @Column(name = "customer_address_zip_code", nullable = false)
    private String customerAddressZipCode;

    @Column(name = "customer_address_label", nullable = false)
    private String customerAddressLabel;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order_Product> orderProducts;
}