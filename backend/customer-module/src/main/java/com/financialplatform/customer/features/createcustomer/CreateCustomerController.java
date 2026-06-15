package com.financialplatform.customer.features.createcustomer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
public class CreateCustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    public CreateCustomerController(CreateCustomerUseCase createCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateCustomerRequest request) {
        CreateCustomerCommand command = new CreateCustomerCommand(
                request.name(),
                request.type(),
                request.document(),
                request.email(),
                null);

        CreateCustomerResult result = createCustomerUseCase.execute(command);
        CreateCustomerResponse response = CreateCustomerResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", response,
                "metadata", Map.of()));
    }
}
