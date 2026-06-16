package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.sharedkernel.domain.Identifier;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
public class UpdateCustomerController {

    private final UpdateCustomerUseCase updateCustomerUseCase;

    public UpdateCustomerController(UpdateCustomerUseCase updateCustomerUseCase) {
        this.updateCustomerUseCase = updateCustomerUseCase;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        UpdateCustomerCommand command = new UpdateCustomerCommand(
                Identifier.of(id),
                request.name(),
                request.email(),
                null);

        UpdateCustomerResult result = updateCustomerUseCase.execute(command);
        UpdateCustomerResponse response = UpdateCustomerResponse.from(result);

        return ResponseEntity.ok(Map.of(
                "data", response,
                "metadata", Map.of()));
    }
}
