package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.token.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListedTokenRepository extends JpaRepository<BlackListedToken, Long> {

    boolean existsByToken(String token);
}

