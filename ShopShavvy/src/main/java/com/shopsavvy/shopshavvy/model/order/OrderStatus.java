package com.shopsavvy.shopshavvy.model.order;

import com.shopsavvy.shopshavvy.model.enums.OrderStatusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private OrderStatusType fromStatus;

    @Enumerated(EnumType.STRING)
    private OrderStatusType toStatus;

    private String transitionNotesComments;

    private LocalDateTime transitionDate;
}
