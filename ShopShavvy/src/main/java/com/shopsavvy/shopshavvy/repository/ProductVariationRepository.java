package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, String> {
}
