package com.creditbook.repository;

import com.creditbook.entity.Transaction;
import com.creditbook.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findTop10ByUserIdOrderByTransactionDateDesc(Long userId);

    Optional<Transaction> findByUserIdAndId(Long userId, Long id);

    List<Transaction> findByUserIdAndCustomerIdOrderByTransactionDateAsc(Long userId, Long customerId);

    List<Transaction> findByUserIdAndCustomerIdOrderByTransactionDateDesc(Long userId, Long customerId);

    long countByUserIdAndCustomerIdAndReversedFalse(Long userId, Long customerId);

    long countByUserIdAndReversedFalse(Long userId);

    // Sum calculations for financial accuracy (ignoring reversed transactions)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.transactionType = :type AND t.reversed = false")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.customer.id = :customerId AND t.transactionType = :type AND t.reversed = false")
    BigDecimal sumAmountByUserIdAndCustomerIdAndType(@Param("userId") Long userId, 
                                                    @Param("customerId") Long customerId, 
                                                    @Param("type") TransactionType type);

    // Flexible filtering query for transactions page
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND " +
           "(:customerId IS NULL OR t.customer.id = :customerId) AND " +
           "(:type IS NULL OR t.transactionType = :type) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(t.customer.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " t.referenceNumber LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> filterTransactions(@Param("userId") Long userId,
                                         @Param("customerId") Long customerId,
                                         @Param("type") TransactionType type,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("query") String query);

    // Sums within date range for reports
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.transactionType = :type AND t.reversed = false AND " +
           "t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(@Param("userId") Long userId,
                                                    @Param("type") TransactionType type,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Fetch latest transaction for a customer
    Optional<Transaction> findFirstByUserIdAndCustomerIdOrderByTransactionDateDesc(Long userId, Long customerId);
}
