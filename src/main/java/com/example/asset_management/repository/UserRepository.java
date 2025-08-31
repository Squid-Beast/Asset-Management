package com.example.asset_management.repository;

import com.example.asset_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.managerId = :managerId")
    List<User> findSubordinatesByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT u FROM User u WHERE u.roleId = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT u FROM User u WHERE u.departmentId = :departmentId")
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}