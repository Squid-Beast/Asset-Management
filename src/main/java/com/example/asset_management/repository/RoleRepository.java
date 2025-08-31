package com.example.asset_management.repository;

import com.example.asset_management.model.Role;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    
    @Query("SELECT * FROM roles WHERE name = :name")
    Optional<Role> findByName(@Param("name") String name);
    
    @Query("SELECT * FROM roles ORDER BY name")
    List<Role> findAllOrderedByName();
    
    @Query("SELECT * FROM roles WHERE id = :id")
    Optional<Role> findById(@Param("id") Long id);
}
