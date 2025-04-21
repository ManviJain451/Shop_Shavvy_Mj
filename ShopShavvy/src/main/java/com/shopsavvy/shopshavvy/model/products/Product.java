package com.shopsavvy.shopshavvy.model.products;

import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.model.categories.Category;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "seller_user_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "is_cancellable", nullable = false)
    private boolean isCancellable;

    @Column(name = "is_returnable", nullable = false)
    private boolean isReturnable;

    @Column(nullable = false)
    private String brand;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariation> productVariations;

    @CreatedDate
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(name = "last_updated_date", nullable = false)
    private LocalDateTime lastUpdated;
}
