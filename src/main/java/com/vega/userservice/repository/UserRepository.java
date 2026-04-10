package com.vega.userservice.repository;

import com.vega.userservice.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u WHERE u.isActive = true AND (
              LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR
              LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
              LOWER(COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
              LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    List<User> searchActiveUsers(@Param("q") String q, Pageable pageable);
}

