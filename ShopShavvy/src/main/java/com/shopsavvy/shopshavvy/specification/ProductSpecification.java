package com.shopsavvy.shopshavvy.specification;

import com.shopsavvy.shopshavvy.model.category.Category;
import com.shopsavvy.shopshavvy.model.product.Product;
import com.shopsavvy.shopshavvy.model.user.Seller;
import jakarta.persistence.criteria.JoinType;
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> getAllByFilterWithCategory(Map<String, String> filter, Category category) {
        Specification<Product> baseSpec = getAllByFilterMap(filter);

        Specification<Product> categorySpec = (root, query, cb) ->
                cb.equal(root.get("category"), category);

        Specification<Product> activeSpec = getAllActive(true);

        return baseSpec.and(categorySpec).and(activeSpec).and(hasVariations());
    }

    public static Specification<Product> getAllByFilterWithSeller(Map<String, String> filter, Seller seller) {
        Specification<Product> baseSpec = getAllByFilterMap(filter);

        Specification<Product> sellerSpec = (root, query, cb) ->
                cb.equal(root.get("seller"), seller);

        Specification<Product> activeSpec = getAllActive(true);

        return baseSpec.and(sellerSpec).and(activeSpec);
    }

    public static Specification<Product> getAllActive(boolean isActive) {
        return (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
    }

    public static Specification<Product> hasVariations() {
        return (root, query, cb) -> {
            root.join("productVariations", JoinType.INNER);
            return cb.conjunction();
        };
    }


}
