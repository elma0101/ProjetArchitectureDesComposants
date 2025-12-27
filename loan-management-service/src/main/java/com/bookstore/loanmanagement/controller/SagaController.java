package com.bookstore.loanmanagement.controller;

import com.bookstore.loanmanagement.saga.LoanReturnSagaData;
import com.bookstore.loanmanagement.saga.LoanReturnSagaOrchestrator;
import com.bookstore.loanmanagement.saga.LoanSagaData;
import com.bookstore.loanmanagement.saga.LoanSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for saga status monitoring and management
 */
@RestController
@RequestMapping("/api/sagas")
@RequiredArgsConstructor
@Slf4j
public class SagaController {
    
    private final LoanSagaOrchestrator loanSagaOrchestrator;
    private final LoanReturnSagaOrchestrator loanReturnSagaOrchestrator;
    
    /**
     * Get loan creation saga status
     * Retrieve the current state of a loan creation saga
     */
    @GetMapping("/loan-creation/{sagaId}")
    public ResponseEntity<LoanSagaData> getLoanCreationSagaStatus(@PathVariable String sagaId) {
        log.info("Retrieving loan creation saga status: sagaId={}", sagaId);
        
        LoanSagaData sagaData = loanSagaOrchestrator.getSagaState(sagaId);
        
        if (sagaData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(sagaData);
    }
    
    /**
     * Get loan return saga status
     * Retrieve the current state of a loan return saga
     */
    @GetMapping("/loan-return/{sagaId}")
    public ResponseEntity<LoanReturnSagaData> getLoanReturnSagaStatus(@PathVariable String sagaId) {
        log.info("Retrieving loan return saga status: sagaId={}", sagaId);
        
        LoanReturnSagaData sagaData = loanReturnSagaOrchestrator.getSagaState(sagaId);
        
        if (sagaData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(sagaData);
    }
}
