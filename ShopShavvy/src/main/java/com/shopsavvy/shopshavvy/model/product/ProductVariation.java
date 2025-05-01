package com.shopsavvy.shopshavvy.model.product;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variations")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("is_deleted = false")
public class ProductVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_active", nullable = false)
    private boolean isActive=true;

    @Column(name = "primary_image", nullable = false)
    private String primaryImage;

    @CreatedDate
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(name = "last_updated_date", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted=false;
}
