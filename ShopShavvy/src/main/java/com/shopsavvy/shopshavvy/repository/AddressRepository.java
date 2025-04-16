package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.users.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {


    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDeleted = true WHERE a.id = :id")
    void deleteById(@Param("id") String id);


}
