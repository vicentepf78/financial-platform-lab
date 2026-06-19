package com.financialplatform.account.infrastructure;

import com.financialplatform.account.domain.AccountNotFoundException;
import com.financialplatform.account.domain.CustomerNotFoundException;
import com.financialplatform.account.domain.InactiveAccountException;
import com.financialplatform.account.domain.InsufficientBalanceException;
import com.financialplatform.account.domain.InvalidAmountException;
import com.financialplatform.account.domain.SameAccountTransferException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.financialplatform.account")
public class AccountExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCustomerNotFound(CustomerNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Customer not found");
        problem.setProperty("type", "customer-not-found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAccountNotFound(AccountNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Account not found");
        problem.setProperty("type", "account-not-found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Insufficient balance");
        problem.setProperty("type", "insufficient-balance");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(SameAccountTransferException.class)
    public ResponseEntity<ProblemDetail> handleSameAccountTransfer(SameAccountTransferException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid transfer");
        problem.setProperty("type", "same-account-transfer");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ProblemDetail> handleInactiveAccount(InactiveAccountException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Account not active");
        problem.setProperty("type", "inactive-account");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAmount(InvalidAmountException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid amount");
        problem.setProperty("type", "invalid-amount");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + "; " + second)
                .orElse("Validation failed");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation failed");
        problem.setProperty("type", "validation-error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
