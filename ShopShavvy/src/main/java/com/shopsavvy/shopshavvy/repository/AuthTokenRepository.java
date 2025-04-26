package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.token.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = 'activation'")
    @Modifying
    void deleteActivationTokenByEmail(@Param("email") String email);

    boolean existsByToken(String token);

    @Query("DELETE FROM AuthToken a WHERE a.userEmail = :email AND a.tokenType = 'reset_password'")
    @Modifying
    void deleteResetPasswordTokenByEmail(@Param("email") String email);

    @Query("DELETE FROM AuthToken a WHERE a.token = :token")
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteByToken(String token);

    void deleteByExpirationTimeBefore(Date expirationTime);


}
