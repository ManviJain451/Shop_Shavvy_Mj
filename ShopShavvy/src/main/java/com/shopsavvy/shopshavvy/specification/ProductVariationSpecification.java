package com.shopsavvy.shopshavvy.specification;

import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductVariationSpecification {

    public static Specification<ProductVariation> getByProductIdAndFilters(
            String productId,
            Map<String, Object> filters
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("product").get("id"), productId));

            if (filters != null) {
                if (filters.containsKey("minPrice")) {
                    Double minPrice = (Double) filters.get("minPrice");
                    if (minPrice != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
                    }
                }

                if (filters.containsKey("maxPrice")) {
                    Double maxPrice = (Double) filters.get("maxPrice");
                    if (maxPrice != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
                    }
                }

                if (filters.containsKey("quantity")) {
                    Double quantity = (Double) filters.get("quantity");
                    if (quantity != null) {
                        predicates.add(cb.equal(root.get("quantity"), quantity));
                    }
                }

                if (filters.containsKey("active")) {
                    Boolean active = (Boolean) filters.get("active");
                    if (active != null) {
                        predicates.add(cb.equal(root.get("isActive"), active));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
