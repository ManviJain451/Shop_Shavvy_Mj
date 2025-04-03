package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.users.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
}
