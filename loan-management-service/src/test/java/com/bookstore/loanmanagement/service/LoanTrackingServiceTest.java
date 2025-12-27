package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.entity.LoanTracking;
import com.bookstore.loanmanagement.repository.LoanTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanTrackingServiceTest {

    @Mock
    private LoanTrackingRepository loanTrackingRepository;

    @InjectMocks
    private LoanTrackingService loanTrackingService;

    @Test
    void recordLoanCreated_Success() {
        // Arrange
        ArgumentCaptor<LoanTracking> captor = ArgumentCaptor.forClass(LoanTracking.class);
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(new LoanTracking());

        // Act
        loanTrackingService.recordLoanCreated(1L, 100L, 200L);

        // Assert
        verify(loanTrackingRepository).save(captor.capture());
        LoanTracking tracking = captor.getValue();
        assertThat(tracking.getLoanId()).isEqualTo(1L);
        assertThat(tracking.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(tracking.getNotes()).contains("user 100", "book 200");
        assertThat(tracking.getChangedBy()).isEqualTo("SYSTEM");
    }

    @Test
    void recordLoanReturned_OnTime() {
        // Arrange
        ArgumentCaptor<LoanTracking> captor = ArgumentCaptor.forClass(LoanTracking.class);
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(new LoanTracking());

        // Act
        loanTrackingService.recordLoanReturned(1L, false);

        // Assert
        verify(loanTrackingRepository).save(captor.capture());
        LoanTracking tracking = captor.getValue();
        assertThat(tracking.getLoanId()).isEqualTo(1L);
        assertThat(tracking.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(tracking.getNotes()).contains("on time");
    }

    @Test
    void recordLoanReturned_Overdue() {
        // Arrange
        ArgumentCaptor<LoanTracking> captor = ArgumentCaptor.forClass(LoanTracking.class);
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(new LoanTracking());

        // Act
        loanTrackingService.recordLoanReturned(1L, true);

        // Assert
        verify(loanTrackingRepository).save(captor.capture());
        LoanTracking tracking = captor.getValue();
        assertThat(tracking.getNotes()).contains("was overdue");
    }

    @Test
    void recordLoanExtension_Success() {
        // Arrange
        LocalDate oldDueDate = LocalDate.now();
        LocalDate newDueDate = LocalDate.now().plusDays(7);
        ArgumentCaptor<LoanTracking> captor = ArgumentCaptor.forClass(LoanTracking.class);
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(new LoanTracking());

        // Act
        loanTrackingService.recordLoanExtension(1L, oldDueDate, newDueDate, 7);

        // Assert
        verify(loanTrackingRepository).save(captor.capture());
        LoanTracking tracking = captor.getValue();
        assertThat(tracking.getLoanId()).isEqualTo(1L);
        assertThat(tracking.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(tracking.getNotes()).contains("extended by 7 days");
    }

    @Test
    void recordLoanOverdue_Success() {
        // Arrange
        ArgumentCaptor<LoanTracking> captor = ArgumentCaptor.forClass(LoanTracking.class);
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(new LoanTracking());

        // Act
        loanTrackingService.recordLoanOverdue(1L);

        // Assert
        verify(loanTrackingRepository).save(captor.capture());
        LoanTracking tracking = captor.getValue();
        assertThat(tracking.getLoanId()).isEqualTo(1L);
        assertThat(tracking.getStatus()).isEqualTo(LoanStatus.OVERDUE);
        assertThat(tracking.getNotes()).contains("marked as overdue");
    }

    @Test
    void getLoanHistory_Success() {
        // Arrange
        LoanTracking tracking1 = new LoanTracking();
        tracking1.setLoanId(1L);
        tracking1.setStatus(LoanStatus.ACTIVE);
        
        LoanTracking tracking2 = new LoanTracking();
        tracking2.setLoanId(1L);
        tracking2.setStatus(LoanStatus.RETURNED);
        
        when(loanTrackingRepository.findByLoanIdOrderByTimestampDesc(1L))
                .thenReturn(Arrays.asList(tracking2, tracking1));

        // Act
        List<LoanTracking> history = loanTrackingService.getLoanHistory(1L);

        // Assert
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(history.get(1).getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }
}
