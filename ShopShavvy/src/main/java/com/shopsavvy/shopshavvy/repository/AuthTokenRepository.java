package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.token.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email")
    @Modifying
    void deleteTokenByEmail(@Param("email") String email);
}
