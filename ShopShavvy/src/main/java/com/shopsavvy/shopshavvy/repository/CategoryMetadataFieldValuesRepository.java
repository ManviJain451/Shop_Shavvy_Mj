package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataField;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValueId;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryMetadataFieldValuesRepository extends JpaRepository<CategoryMetadataFieldValues, CategoryMetadataFieldValueId> {

    @Query("SELECT cmfv FROM CategoryMetadataFieldValues cmfv WHERE cmfv.category.categoryId = :categoryId")
    List<CategoryMetadataFieldValues> findMetadataFieldByCategoryId(@Param("categoryId") String categoryId);

}
