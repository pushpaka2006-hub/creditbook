package com.creditbook.repository;

import com.creditbook.entity.Customer;
import com.creditbook.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByUserIdOrderByFullNameAsc(Long userId);

    Page<Customer> findByUserId(Long userId, Pageable pageable);

    Optional<Customer> findByUserIdAndId(Long userId, Long customerId);

    boolean existsByUserIdAndMobile(Long userId, String mobile);

    boolean existsByUserIdAndMobileAndIdNot(Long userId, String mobile, Long id);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, CustomerStatus status);

    @Query("SELECT c FROM Customer c WHERE c.user.id = :userId AND " +
           "(LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "c.mobile LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Customer> searchCustomers(@Param("userId") Long userId, @Param("query") String query);

    @Query("SELECT c FROM Customer c WHERE c.user.id = :userId AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR c.mobile LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY c.fullName ASC")
    List<Customer> filterCustomers(@Param("userId") Long userId, 
                                   @Param("status") CustomerStatus status, 
                                   @Param("query") String query);
}
