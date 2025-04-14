package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.users.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
}
