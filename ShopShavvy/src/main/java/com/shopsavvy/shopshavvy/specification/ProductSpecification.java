package com.shopsavvy.shopshavvy.specification;

import com.shopsavvy.shopshavvy.dto.productDto.ProductFilterDTO;
import com.shopsavvy.shopshavvy.model.products.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
public class ProductSpecification {

    public static Specification<Product> getAllByFilter(ProductFilterDTO filterDTO){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDTO.getName() != null)
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filterDTO.getName() + "%"));
            if (filterDTO.getBrand() != null)
                predicates.add(cb.like(cb.lower(root.get("brand")), "%" + filterDTO.getBrand() + "%"));
            if (filterDTO.getDescription() != null)
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + filterDTO.getDescription() + "%"));
            if (filterDTO.getCategoryId() != null)
                predicates.add(cb.equal(root.get("category").get("categoryId"), filterDTO.getCategoryId()));
            if (filterDTO.getSellerId() != null)
                predicates.add(cb.equal(root.get("seller").get("id"), filterDTO.getSellerId()));
            if(filterDTO.isActive())
                predicates.add(cb.equal(root.get("isActive"), filterDTO.isActive()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
