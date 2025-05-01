package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByParentCategoryIsNull();

    Page<Category> findByNameContainingIgnoreCase(String query, Pageable pageable);

}
