package com.bidpro.dao;

import com.bidpro.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminDao extends JpaRepository<Admin, String> {
    
    Optional<Admin> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Admin a WHERE a.email = :email AND a.status = 'ACTIVE'")
    Optional<Admin> findActiveByEmail(String email);
}