package com.bookstore.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatistics {
    private Long activeLoans;
    private Long overdueLoans;
    private Long returnedLoans;
    private Long totalLoans;
    
    public LoanStatistics(Long activeLoans, Long overdueLoans, Long returnedLoans) {
        this.activeLoans = activeLoans != null ? activeLoans : 0L;
        this.overdueLoans = overdueLoans != null ? overdueLoans : 0L;
        this.returnedLoans = returnedLoans != null ? returnedLoans : 0L;
        this.totalLoans = this.activeLoans + this.overdueLoans + this.returnedLoans;
    }
}
