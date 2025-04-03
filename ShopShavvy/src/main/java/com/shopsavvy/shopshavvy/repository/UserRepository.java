package com.shopsavvy.shopshavvy.repository;

import com.shopsavvy.shopshavvy.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    public User findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("select COUNT(*) > 0 from Seller where LOWER(companyName) = LOWER(:companyName)")
    boolean existsByCompanyName(@Param("companyName") String companyName);


    @Query("select COUNT(*) > 0 from Seller where gst = :gst")
    boolean existsByGst(@Param("gst") String gst);

    @Query("SELECT u.isActive FROM User u WHERE u.email = :email")
    Boolean findIsActiveByEmail(@Param("email") String email);

}
