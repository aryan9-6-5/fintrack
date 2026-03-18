package com.fintrack.transaction;

import com.fintrack.transaction.dto.CategorySummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByUserId(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(AVG(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId")
    BigDecimal getAverageAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.fintrack.transaction.dto.CategorySummaryResponse(t.category, SUM(t.amount), COUNT(t)) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND (:month IS NULL OR MONTH(t.createdAt) = :month) " +
           "AND (:year IS NULL OR YEAR(t.createdAt) = :year) " +
           "GROUP BY t.category")
    List<CategorySummaryResponse> getSummary(@Param("userId") Long userId,
                                             @Param("month") Integer month,
                                             @Param("year") Integer year);
}
