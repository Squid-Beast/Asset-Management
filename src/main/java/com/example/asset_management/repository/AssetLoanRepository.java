package com.example.asset_management.repository;

import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.AssetLoan.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetLoanRepository extends JpaRepository<AssetLoan, Long> {
    
    @Query("SELECT al FROM AssetLoan al WHERE al.userId = :userId")
    List<AssetLoan> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.userId = :userId AND al.status = :status")
    List<AssetLoan> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") LoanStatus status);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.assetId = :assetId")
    List<AssetLoan> findByAssetId(@Param("assetId") Long assetId);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.status = :status")
    List<AssetLoan> findByStatus(@Param("status") LoanStatus status);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.status = 'pending_approval'")
    List<AssetLoan> findPendingApprovals();
    
    @Query("SELECT al FROM AssetLoan al WHERE al.assignedById = :assignedById")
    List<AssetLoan> findByAssignedById(@Param("assignedById") Long assignedById);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.dueAt BETWEEN :startDate AND :endDate")
    List<AssetLoan> findLoansDueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.dueAt < :date AND al.status IN ('loaned', 'overdue')")
    List<AssetLoan> findOverdueLoans(@Param("date") LocalDateTime date);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.dueAt BETWEEN :now AND :futureDate AND al.status = 'loaned'")
    List<AssetLoan> findLoansDueSoon(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.assetId = :assetId AND al.status IN ('loaned', 'pending_approval')")
    Optional<AssetLoan> findActiveLoanByAssetId(@Param("assetId") Long assetId);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.userId IN (:userIds)")
    List<AssetLoan> findByUserIdIn(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT al FROM AssetLoan al WHERE al.userId IN (:userIds) AND al.status = :status")
    List<AssetLoan> findByUserIdInAndStatus(@Param("userIds") List<Long> userIds, @Param("status") LoanStatus status);
}