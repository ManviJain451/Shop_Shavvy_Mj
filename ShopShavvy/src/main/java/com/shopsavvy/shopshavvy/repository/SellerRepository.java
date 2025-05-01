package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.user.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {

    Optional<Seller> findByEmail(String email);
    Page<Seller> findByEmailContainingIgnoreCase(String email, Pageable pageable);

}
