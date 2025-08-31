package com.example.asset_management.repository;

import com.example.asset_management.model.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    @Query("SELECT * FROM users WHERE username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    @Query("SELECT * FROM users WHERE email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT * FROM users WHERE username = :username AND is_active = true")
    Optional<User> findActiveByUsername(@Param("username") String username);
    
    @Query("SELECT * FROM users WHERE manager_id = :managerId")
    List<User> findSubordinatesByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT * FROM users WHERE role_id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT * FROM users WHERE department_id = :departmentId")
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT * FROM users WHERE is_active = true")
    List<User> findAllActiveUsers();
    
    @Query("SELECT * FROM users WHERE id = :id AND is_active = true")
    Optional<User> findActiveById(@Param("id") Long id);
    
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean existsByUsername(@Param("username") String username);
    
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean existsByEmail(@Param("email") String email);
    
    @Query("SELECT * FROM users WHERE id = :id")
    Optional<User> findById(@Param("id") Long id);
    
    @Query("SELECT * FROM users WHERE id IN (:ids)")
    List<User> findAllById(@Param("ids") List<Long> ids);
}
