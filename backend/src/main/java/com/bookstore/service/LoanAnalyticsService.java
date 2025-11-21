package com.bookstore.service;

import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.LoanRepository;
import com.bookstore.repository.LoanTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loan analytics and reporting
 */
@Service
@Transactional(readOnly = true)
public class LoanAnalyticsService {
    
    private final LoanRepository loanRepository;
    private final LoanTrackingRepository loanTrackingRepository;
    
    @Autowired
    public LoanAnalyticsService(LoanRepository loanRepository, LoanTrackingRepository loanTrackingRepository) {
        this.loanRepository = loanRepository;
        this.loanTrackingRepository = loanTrackingRepository;
    }
    
    /**
     * Get comprehensive loan statistics
     */
    public LoanAnalytics getLoanAnalytics() {
        LoanAnalytics analytics = new LoanAnalytics();
        
        // Basic counts
        analytics.setTotalLoans(loanRepository.count());
        analytics.setActiveLoans(loanRepository.countByStatus(LoanStatus.ACTIVE));
        analytics.setOverdueLoans(loanRepository.countByStatus(LoanStatus.OVERDUE));
        analytics.setReturnedLoans(loanRepository.countByStatus(LoanStatus.RETURNED));
        
        // Calculate rates
        if (analytics.getTotalLoans() > 0) {
            analytics.setOverdueRate((double) analytics.getOverdueLoans() / analytics.getTotalLoans() * 100);
            analytics.setReturnRate((double) analytics.getReturnedLoans() / analytics.getTotalLoans() * 100);
        }
        
        // Average loan duration
        analytics.setAverageLoanDuration(calculateAverageLoanDuration());
        
        // Most borrowed books
        analytics.setMostBorrowedBooks(getMostBorrowedBooksAnalytics());
        
        // Most active borrowers
        analytics.setMostActiveBorrowers(getMostActiveBorrowersAnalytics());
        
        // Loan trends
        analytics.setLoanTrends(getLoanTrends());
        
        // Notification statistics
        List<Object[]> notificationStatsRaw = loanTrackingRepository.getNotificationStatistics();
        Map<String, Long> notificationStats = new HashMap<>();
        for (Object[] stat : notificationStatsRaw) {
            notificationStats.put((String) stat[0], (Long) stat[1]);
        }
        analytics.setNotificationStats(notificationStats);
        
        return analytics;
    }
    
    /**
     * Get loan statistics for a specific date range
     */
    public LoanAnalytics getLoanAnalyticsForDateRange(LocalDate startDate, LocalDate endDate) {
        LoanAnalytics analytics = new LoanAnalytics();
        
        // Get loans in date range
        Page<Loan> loansInRange = loanRepository.findByLoanDateBetween(startDate, endDate, Pageable.unpaged());
        List<Loan> loans = loansInRange.getContent();
        
        analytics.setTotalLoans((long) loans.size());
        analytics.setActiveLoans(loans.stream().mapToLong(l -> l.getStatus() == LoanStatus.ACTIVE ? 1 : 0).sum());
        analytics.setOverdueLoans(loans.stream().mapToLong(l -> l.getStatus() == LoanStatus.OVERDUE ? 1 : 0).sum());
        analytics.setReturnedLoans(loans.stream().mapToLong(l -> l.getStatus() == LoanStatus.RETURNED ? 1 : 0).sum());
        
        // Calculate rates
        if (analytics.getTotalLoans() > 0) {
            analytics.setOverdueRate((double) analytics.getOverdueLoans() / analytics.getTotalLoans() * 100);
            analytics.setReturnRate((double) analytics.getReturnedLoans() / analytics.getTotalLoans() * 100);
        }
        
        // Average loan duration for returned books in range
        analytics.setAverageLoanDuration(calculateAverageLoanDurationForLoans(loans));
        
        return analytics;
    }
    
    /**
     * Get overdue loan analysis
     */
    public OverdueLoanAnalysis getOverdueLoanAnalysis() {
        OverdueLoanAnalysis analysis = new OverdueLoanAnalysis();
        
        // Get both ACTIVE loans that are overdue and loans with OVERDUE status
        Page<Loan> activeOverdueLoans = loanRepository.findOverdueLoans(Pageable.unpaged());
        Page<Loan> overdueStatusLoans = loanRepository.findByStatus(LoanStatus.OVERDUE, Pageable.unpaged());
        
        List<Loan> allOverdueLoans = new java.util.ArrayList<>();
        allOverdueLoans.addAll(activeOverdueLoans.getContent());
        allOverdueLoans.addAll(overdueStatusLoans.getContent());
        
        analysis.setTotalOverdueLoans((long) allOverdueLoans.size());
        
        if (!allOverdueLoans.isEmpty()) {
            // Calculate average days overdue - need custom logic for OVERDUE status loans
            double avgDaysOverdue = allOverdueLoans.stream()
                .mapToLong(this::calculateDaysOverdue)
                .average()
                .orElse(0.0);
            analysis.setAverageDaysOverdue(avgDaysOverdue);
            
            // Find longest overdue
            long maxDaysOverdue = allOverdueLoans.stream()
                .mapToLong(this::calculateDaysOverdue)
                .max()
                .orElse(0);
            analysis.setLongestOverdueDays(maxDaysOverdue);
            
            // Group by days overdue ranges
            Map<String, Long> overdueRanges = new HashMap<>();
            overdueRanges.put("1-7 days", allOverdueLoans.stream().filter(l -> calculateDaysOverdue(l) <= 7 && calculateDaysOverdue(l) > 0).count());
            overdueRanges.put("8-14 days", allOverdueLoans.stream().filter(l -> calculateDaysOverdue(l) > 7 && calculateDaysOverdue(l) <= 14).count());
            overdueRanges.put("15-30 days", allOverdueLoans.stream().filter(l -> calculateDaysOverdue(l) > 14 && calculateDaysOverdue(l) <= 30).count());
            overdueRanges.put("30+ days", allOverdueLoans.stream().filter(l -> calculateDaysOverdue(l) > 30).count());
            
            analysis.setOverdueRanges(overdueRanges);
        }
        
        return analysis;
    }
    
    /**
     * Calculate days overdue for any loan, regardless of status
     */
    private long calculateDaysOverdue(Loan loan) {
        if (loan.getDueDate().isBefore(LocalDate.now())) {
            return LocalDate.now().toEpochDay() - loan.getDueDate().toEpochDay();
        }
        return 0;
    }
    
    /**
     * Get borrower behavior analysis
     */
    public BorrowerAnalysis getBorrowerAnalysis() {
        BorrowerAnalysis analysis = new BorrowerAnalysis();
        
        // Get unique borrowers count
        List<String> uniqueBorrowers = loanRepository.findAll().stream()
            .map(Loan::getBorrowerEmail)
            .distinct()
            .collect(Collectors.toList());
        
        analysis.setTotalUniqueBorrowers((long) uniqueBorrowers.size());
        
        // Calculate average loans per borrower
        if (!uniqueBorrowers.isEmpty()) {
            double avgLoansPerBorrower = (double) loanRepository.count() / uniqueBorrowers.size();
            analysis.setAverageLoansPerBorrower(avgLoansPerBorrower);
        }
        
        // Get repeat borrowers (borrowers with more than 1 loan)
        Map<String, Long> borrowerLoanCounts = loanRepository.findAll().stream()
            .collect(Collectors.groupingBy(Loan::getBorrowerEmail, Collectors.counting()));
        
        long repeatBorrowers = borrowerLoanCounts.values().stream()
            .mapToLong(count -> count > 1 ? 1 : 0)
            .sum();
        
        analysis.setRepeatBorrowers(repeatBorrowers);
        
        if (analysis.getTotalUniqueBorrowers() > 0) {
            analysis.setRepeatBorrowerRate((double) repeatBorrowers / analysis.getTotalUniqueBorrowers() * 100);
        }
        
        return analysis;
    }
    
    /**
     * Get loan trends over time
     */
    private Map<String, Object> getLoanTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // Get loans for last 12 months
        LocalDate startDate = LocalDate.now().minusMonths(12);
        Page<Loan> recentLoans = loanRepository.findByLoanDateBetween(startDate, LocalDate.now(), Pageable.unpaged());
        
        // Group by month
        Map<String, Long> monthlyLoans = recentLoans.getContent().stream()
            .collect(Collectors.groupingBy(
                loan -> loan.getLoanDate().getYear() + "-" + String.format("%02d", loan.getLoanDate().getMonthValue()),
                Collectors.counting()
            ));
        
        trends.put("monthlyLoans", monthlyLoans);
        
        // Calculate trend direction
        List<Long> monthlyCounts = new ArrayList<>(monthlyLoans.values());
        if (monthlyCounts.size() >= 2) {
            long recentAvg = monthlyCounts.subList(Math.max(0, monthlyCounts.size() - 3), monthlyCounts.size())
                .stream().mapToLong(Long::longValue).sum() / Math.min(3, monthlyCounts.size());
            long earlierAvg = monthlyCounts.subList(0, Math.min(3, monthlyCounts.size()))
                .stream().mapToLong(Long::longValue).sum() / Math.min(3, monthlyCounts.size());
            
            if (recentAvg > earlierAvg) {
                trends.put("trendDirection", "INCREASING");
            } else if (recentAvg < earlierAvg) {
                trends.put("trendDirection", "DECREASING");
            } else {
                trends.put("trendDirection", "STABLE");
            }
        }
        
        return trends;
    }
    
    /**
     * Calculate average loan duration
     */
    private Double calculateAverageLoanDuration() {
        Page<Loan> returnedLoans = loanRepository.findByStatus(LoanStatus.RETURNED, Pageable.unpaged());
        return calculateAverageLoanDurationForLoans(returnedLoans.getContent());
    }
    
    /**
     * Calculate average loan duration for specific loans
     */
    private Double calculateAverageLoanDurationForLoans(List<Loan> loans) {
        List<Loan> returnedLoans = loans.stream()
            .filter(loan -> loan.getStatus() == LoanStatus.RETURNED && loan.getReturnDate() != null)
            .collect(Collectors.toList());
        
        if (returnedLoans.isEmpty()) {
            return 0.0;
        }
        
        return returnedLoans.stream()
            .mapToLong(loan -> ChronoUnit.DAYS.between(loan.getLoanDate(), loan.getReturnDate()))
            .average()
            .orElse(0.0);
    }
    
    /**
     * Get most borrowed books analytics
     */
    private List<Map<String, Object>> getMostBorrowedBooksAnalytics() {
        Page<Object[]> mostBorrowed = loanRepository.findMostBorrowedBooks(PageRequest.of(0, 10));
        
        return mostBorrowed.getContent().stream()
            .map(row -> {
                Map<String, Object> book = new HashMap<>();
                book.put("bookId", row[0]);
                book.put("title", row[1]);
                book.put("loanCount", row[2]);
                return book;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get most active borrowers analytics
     */
    private List<Map<String, Object>> getMostActiveBorrowersAnalytics() {
        Page<Object[]> mostActive = loanRepository.findMostActiveBorrowers(PageRequest.of(0, 10));
        
        return mostActive.getContent().stream()
            .map(row -> {
                Map<String, Object> borrower = new HashMap<>();
                borrower.put("email", row[0]);
                borrower.put("name", row[1]);
                borrower.put("loanCount", row[2]);
                return borrower;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get daily notification statistics for the last N days
     */
    public Map<String, Long> getDailyNotificationStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> dailyCounts = loanTrackingRepository.getDailyNotificationCounts(startDate);
        
        return dailyCounts.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
    }
    
    // Analytics data classes
    public static class LoanAnalytics {
        private Long totalLoans;
        private Long activeLoans;
        private Long overdueLoans;
        private Long returnedLoans;
        private Double overdueRate;
        private Double returnRate;
        private Double averageLoanDuration;
        private List<Map<String, Object>> mostBorrowedBooks;
        private List<Map<String, Object>> mostActiveBorrowers;
        private Map<String, Object> loanTrends;
        private Map<String, Long> notificationStats;
        
        // Getters and setters
        public Long getTotalLoans() { return totalLoans; }
        public void setTotalLoans(Long totalLoans) { this.totalLoans = totalLoans; }
        
        public Long getActiveLoans() { return activeLoans; }
        public void setActiveLoans(Long activeLoans) { this.activeLoans = activeLoans; }
        
        public Long getOverdueLoans() { return overdueLoans; }
        public void setOverdueLoans(Long overdueLoans) { this.overdueLoans = overdueLoans; }
        
        public Long getReturnedLoans() { return returnedLoans; }
        public void setReturnedLoans(Long returnedLoans) { this.returnedLoans = returnedLoans; }
        
        public Double getOverdueRate() { return overdueRate; }
        public void setOverdueRate(Double overdueRate) { this.overdueRate = overdueRate; }
        
        public Double getReturnRate() { return returnRate; }
        public void setReturnRate(Double returnRate) { this.returnRate = returnRate; }
        
        public Double getAverageLoanDuration() { return averageLoanDuration; }
        public void setAverageLoanDuration(Double averageLoanDuration) { this.averageLoanDuration = averageLoanDuration; }
        
        public List<Map<String, Object>> getMostBorrowedBooks() { return mostBorrowedBooks; }
        public void setMostBorrowedBooks(List<Map<String, Object>> mostBorrowedBooks) { this.mostBorrowedBooks = mostBorrowedBooks; }
        
        public List<Map<String, Object>> getMostActiveBorrowers() { return mostActiveBorrowers; }
        public void setMostActiveBorrowers(List<Map<String, Object>> mostActiveBorrowers) { this.mostActiveBorrowers = mostActiveBorrowers; }
        
        public Map<String, Object> getLoanTrends() { return loanTrends; }
        public void setLoanTrends(Map<String, Object> loanTrends) { this.loanTrends = loanTrends; }
        
        public Map<String, Long> getNotificationStats() { return notificationStats; }
        public void setNotificationStats(Map<String, Long> notificationStats) { this.notificationStats = notificationStats; }
    }
    
    public static class OverdueLoanAnalysis {
        private Long totalOverdueLoans;
        private Double averageDaysOverdue;
        private Long longestOverdueDays;
        private Map<String, Long> overdueRanges;
        
        // Getters and setters
        public Long getTotalOverdueLoans() { return totalOverdueLoans; }
        public void setTotalOverdueLoans(Long totalOverdueLoans) { this.totalOverdueLoans = totalOverdueLoans; }
        
        public Double getAverageDaysOverdue() { return averageDaysOverdue; }
        public void setAverageDaysOverdue(Double averageDaysOverdue) { this.averageDaysOverdue = averageDaysOverdue; }
        
        public Long getLongestOverdueDays() { return longestOverdueDays; }
        public void setLongestOverdueDays(Long longestOverdueDays) { this.longestOverdueDays = longestOverdueDays; }
        
        public Map<String, Long> getOverdueRanges() { return overdueRanges; }
        public void setOverdueRanges(Map<String, Long> overdueRanges) { this.overdueRanges = overdueRanges; }
    }
    
    public static class BorrowerAnalysis {
        private Long totalUniqueBorrowers;
        private Double averageLoansPerBorrower;
        private Long repeatBorrowers;
        private Double repeatBorrowerRate;
        
        // Getters and setters
        public Long getTotalUniqueBorrowers() { return totalUniqueBorrowers; }
        public void setTotalUniqueBorrowers(Long totalUniqueBorrowers) { this.totalUniqueBorrowers = totalUniqueBorrowers; }
        
        public Double getAverageLoansPerBorrower() { return averageLoansPerBorrower; }
        public void setAverageLoansPerBorrower(Double averageLoansPerBorrower) { this.averageLoansPerBorrower = averageLoansPerBorrower; }
        
        public Long getRepeatBorrowers() { return repeatBorrowers; }
        public void setRepeatBorrowers(Long repeatBorrowers) { this.repeatBorrowers = repeatBorrowers; }
        
        public Double getRepeatBorrowerRate() { return repeatBorrowerRate; }
        public void setRepeatBorrowerRate(Double repeatBorrowerRate) { this.repeatBorrowerRate = repeatBorrowerRate; }
    }
}