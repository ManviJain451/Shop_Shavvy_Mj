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

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.companyName) = LOWER(:companyName)")
    public Boolean existsByCompanyNameIgnoreCase(@Param("companyName") String companyName);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.gst = :gst")
    public Boolean existsByGst(@Param("gst") String gst);

}
