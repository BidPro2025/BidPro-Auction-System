package com.bidpro.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bidpro.entities.User;
import com.bidpro.entities.User.Role;
import com.bidpro.entities.User.Status;

public interface UserDao extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    List<User> findByStatus(Status status);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:keyword% OR u.lastName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
}
