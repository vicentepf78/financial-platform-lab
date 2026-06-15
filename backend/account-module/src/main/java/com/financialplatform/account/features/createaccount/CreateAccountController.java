package com.financialplatform.account.features.createaccount;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class CreateAccountController {

    private final CreateAccountUseCase createAccountUseCase;

    public CreateAccountController(CreateAccountUseCase createAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateAccountRequest request) {
        CreateAccountCommand command = new CreateAccountCommand(request.customerId(), null);

        CreateAccountResult result = createAccountUseCase.execute(command);
        CreateAccountResponse response = CreateAccountResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", response,
                "metadata", Map.of()));
    }
}
