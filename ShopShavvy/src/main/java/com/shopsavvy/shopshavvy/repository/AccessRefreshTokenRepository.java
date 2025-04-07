package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.token.AccessRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRefreshTokenRepository extends JpaRepository<AccessRefreshToken, Long> {

    Optional<AccessRefreshToken> findByAccessToken(String accessToken);

    @Modifying
    void deleteByUserEmail(String userEmail);

    Optional<AccessRefreshToken> findByRefreshToken(String refreshToken);

    @Query("SELECT COUNT(a) > 0 FROM AccessRefreshToken a WHERE a.accessToken = :accessToken")
    boolean existsByAccessToken(@Param("accessToken") String accessToken);

    @Query("SELECT COUNT(a) > 0 FROM AccessRefreshToken a WHERE a.refreshToken = :refreshToken")
    boolean existsByRefreshToken(@Param("refreshToken") String refreshToken);
}
