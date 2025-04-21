package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    boolean existsByCategory(Category category);
    List<Product> findByCategoryIn(Collection<Category> categories);
    List<Product> findByNameAndCategoryAndBrandAndSeller(String name, Category category, String brand, User seller);
    List<Product> findBySeller(Seller seller);

}
