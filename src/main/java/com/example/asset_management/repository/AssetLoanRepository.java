package com.example.asset_management.repository;

import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.AssetLoan.LoanStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetLoanRepository extends CrudRepository<AssetLoan, Long> {
    
    @Query("SELECT * FROM asset_loans WHERE user_id = :userId")
    List<AssetLoan> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT * FROM asset_loans WHERE user_id = :userId AND status = :status")
    List<AssetLoan> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") LoanStatus status);
    
    @Query("SELECT * FROM asset_loans WHERE asset_id = :assetId")
    List<AssetLoan> findByAssetId(@Param("assetId") Long assetId);
    
    @Query("SELECT * FROM asset_loans WHERE status = :status")
    List<AssetLoan> findByStatus(@Param("status") LoanStatus status);
    
    @Query("SELECT * FROM asset_loans WHERE status = 'PENDING_APPROVAL'")
    List<AssetLoan> findPendingApprovals();
    
    @Query("SELECT * FROM asset_loans WHERE assigned_by_id = :assignedById")
    List<AssetLoan> findByAssignedById(@Param("assignedById") Long assignedById);
    
    @Query("SELECT * FROM asset_loans WHERE due_at BETWEEN :startDate AND :endDate")
    List<AssetLoan> findLoansDueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT * FROM asset_loans WHERE due_at < :date AND status IN ('LOANED', 'OVERDUE')")
    List<AssetLoan> findOverdueLoans(@Param("date") LocalDateTime date);
    
    @Query("SELECT * FROM asset_loans WHERE due_at BETWEEN :now AND :futureDate AND status = 'LOANED'")
    List<AssetLoan> findLoansDueSoon(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);
    
    @Query("SELECT * FROM asset_loans WHERE id = :id")
    Optional<AssetLoan> findById(@Param("id") Long id);
    
    @Query("SELECT * FROM asset_loans WHERE asset_id = :assetId AND status IN ('LOANED', 'PENDING_APPROVAL')")
    Optional<AssetLoan> findActiveLoanByAssetId(@Param("assetId") Long assetId);
    
    @Query("SELECT * FROM asset_loans WHERE user_id IN (:userIds)")
    List<AssetLoan> findByUserIdIn(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT * FROM asset_loans WHERE user_id IN (:userIds) AND status = :status")
    List<AssetLoan> findByUserIdInAndStatus(@Param("userIds") List<Long> userIds, @Param("status") LoanStatus status);
}
