package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.token.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = 'activation'")
    @Modifying
    void deleteActivationTokenByEmail(@Param("email") String email);

    boolean existsByToken(String token);

    @Query("SELECT COUNT(a) > 0 FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = 'reset_password'")
    boolean existsResetPasswordTokenByEmail(@Param("email") String email);

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = 'reset_password'")
    @Modifying
    void deleteResetPasswordTokenByEmail(@Param("email") String email);

    @Query("DELETE FROM AuthToken a WHERE a.token = :token")
    @Modifying
    void deleteByToken(String token);

    void deleteByExpirationTimeBefore(Date expirationTime);


}
