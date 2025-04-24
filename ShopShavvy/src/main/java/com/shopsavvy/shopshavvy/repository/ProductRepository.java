package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    boolean existsByCategory(Category category);

    List<Product> findByCategoryIn(Collection<Category> categories);

    List<Product> findByNameAndCategoryAndBrandAndSeller(String name, Category category, String brand, User seller);

    boolean existsByNameAndBrandAndCategoryAndSellerAndIdNot(
            String name,
            String brand,
            Category category,
            User seller,
            String productId
    );

}
