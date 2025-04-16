package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.model.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select COUNT(*) > 0 from Seller where LOWER(companyName) = LOWER(:companyName)")
    boolean existsByCompanyName(@Param("companyName") String companyName);


    @Query("select COUNT(*) > 0 from Seller where gst = :gst")
    boolean existsByGst(@Param("gst") String gst);

    @Query("SELECT u.isActive FROM User u WHERE u.email = :email")
    boolean findIsActiveByEmail(@Param("email") String email);

    @Query("SELECT u.passwordUpdateDate FROM User u WHERE u.email = :email")
    LocalDateTime findPasswordUpdateDateByEmail(@Param("email") String email);

    Optional<User> findByEmailAndRoles(String email, Set<Role> roles);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.authority = :role AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<User> findByEmailContainingIgnoreCaseAndRoles(@Param("email") String email, @Param("role") String role, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.authority = :role")
    Page<User> findByRoles(@Param("role") String role, Pageable pageable);

    List<User> findAllByIsLocked(boolean isLocked);

    Optional<User> findById(String id);

    @Query("SELECT u FROM User u WHERE u.passwordUpdateDate < ?1 AND u.isExpired = false")
    List<User> findByPasswordLastUpdateDateBeforeAndExpiredFalse(LocalDateTime threshold);

}
