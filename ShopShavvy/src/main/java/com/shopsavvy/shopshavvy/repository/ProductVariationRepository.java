package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, String> {

    Page<ProductVariation> findAll(Specification<ProductVariation> specification, Pageable pageable);

}
