package com.example.asset_management.repository;

import com.example.asset_management.model.Department;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long> {
    
    @Query("SELECT * FROM departments WHERE name = :name")
    Optional<Department> findByName(@Param("name") String name);
    
    @Query("SELECT * FROM departments ORDER BY name")
    List<Department> findAllOrderedByName();
    
    @Query("SELECT * FROM departments WHERE id = :id")
    Optional<Department> findById(@Param("id") Long id);
}
