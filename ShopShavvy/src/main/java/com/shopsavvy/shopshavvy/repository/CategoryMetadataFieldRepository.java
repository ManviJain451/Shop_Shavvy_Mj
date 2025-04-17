package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField, String> {
    boolean existsByName(String name);

    Page<CategoryMetadataField> findByNameContainingIgnoreCase(String query, Pageable pageable);
}
