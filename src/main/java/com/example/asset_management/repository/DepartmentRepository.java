package com.example.asset_management.repository;

import com.example.asset_management.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    @Query("SELECT d FROM Department d WHERE d.name = :name")
    Optional<Department> findByName(@Param("name") String name);
    
    @Query("SELECT d FROM Department d ORDER BY d.name")
    List<Department> findAllOrderedByName();
}