package com.financialplatform.account.features.transfermoney;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financialplatform.account.infrastructure.TransferMoneyTransactionalBoundary;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferMoneyController {

    private final TransferMoneyTransactionalBoundary transferMoneyTransactionalBoundary;

    public TransferMoneyController(TransferMoneyTransactionalBoundary transferMoneyTransactionalBoundary) {
        this.transferMoneyTransactionalBoundary = transferMoneyTransactionalBoundary;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> transfer(@Valid @RequestBody TransferMoneyRequest request) {
        TransferMoneyCommand command = new TransferMoneyCommand(
                request.originAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.correlationId(),
                request.idempotencyKey(),
                null);

        TransferMoneyResult result = transferMoneyTransactionalBoundary.execute(command);
        TransferMoneyResponse response = TransferMoneyResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", response,
                "metadata", Map.of()));
    }
}
