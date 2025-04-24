package com.shopsavvy.shopshavvy.specification;

import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.users.Seller;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductSpecification {

    public static Specification<Product> getAllByFilterMap(Map<String, String> filterMap) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterMap.containsKey("name")) {
                String name = filterMap.get("name").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name + "%"));
            }

            if (filterMap.containsKey("brand")) {
                String brand = filterMap.get("brand").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("brand")), "%" + brand + "%"));
            }

            if (filterMap.containsKey("description")) {
                String description = filterMap.get("description").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description + "%"));
            }

            if (filterMap.containsKey("categoryId")) {
                String categoryId = filterMap.get("categoryId");
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }

            if (filterMap.containsKey("sellerId")) {
                String sellerId = filterMap.get("sellerId");
                predicates.add(cb.equal(root.get("seller").get("id"), sellerId));
            }

            if (filterMap.containsKey("active")) {
                boolean isActive = Boolean.parseBoolean(filterMap.get("active"));
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> getAllByFilterWithCategory(Map<String, String> filter, Category category) {
        Specification<Product> baseSpec = getAllByFilterMap(filter);
        Specification<Product> categorySpec = (root, query, cb) ->
                cb.equal(root.get("category"), category);

        return baseSpec.and(categorySpec);
    }

    public static Specification<Product> getAllByFilterWithSeller(Map<String, String> filter, Seller seller) {
        Specification<Product> baseSpec = getAllByFilterMap(filter);
        Specification<Product> categorySpec = (root, query, cb) ->
                cb.equal(root.get("category"), seller);

        return baseSpec.and(categorySpec);
    }

}
