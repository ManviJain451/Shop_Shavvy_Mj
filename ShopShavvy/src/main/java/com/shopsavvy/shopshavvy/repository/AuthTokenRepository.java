package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.token.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email")
    @Modifying
    void deleteTokenByEmail(@Param("email") String email);

    Optional<AuthToken> findByToken(String token);

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = com.shopsavvy.shopshavvy.model.token.TokenType.ACCESS")
    @Modifying
    void deleteAccessTokenByEmail(@Param("email") String email);

    boolean existsByToken(String token);

    @Query("SELECT COUNT(a) > 0 FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = com.shopsavvy.shopshavvy.model.token.TokenType.FORGOT_PASSWORD")
    boolean existsForgotPasswordTokenByEmail(@Param("email") String email);

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = com.shopsavvy.shopshavvy.model.token.TokenType.FORGOT_PASSWORD")
    @Modifying
    void deleteForgotPasswordTokenByEmail(@Param("email") String email);

    @Query("DELETE FROM AuthToken a WHERE a.token = :token")
    @Modifying
    void deleteByToken(String token);

}
