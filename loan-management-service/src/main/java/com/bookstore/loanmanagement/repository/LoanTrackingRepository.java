package com.bookstore.loanmanagement.repository;

import com.bookstore.loanmanagement.entity.LoanTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanTrackingRepository extends JpaRepository<LoanTracking, Long> {

    List<LoanTracking> findByLoanIdOrderByTimestampDesc(Long loanId);
}
