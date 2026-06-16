package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customers")
public class QueryCustomersController {

    private final QueryCustomersUseCase queryCustomersUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;

    public QueryCustomersController(
            QueryCustomersUseCase queryCustomersUseCase,
            GetCustomerByIdUseCase getCustomerByIdUseCase) {
        this.queryCustomersUseCase = queryCustomersUseCase;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCustomers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) String document) {
        QueryCustomersQuery query = new QueryCustomersQuery(
                Optional.ofNullable(page),
                Optional.ofNullable(size),
                Optional.ofNullable(name),
                Optional.ofNullable(type),
                Optional.ofNullable(document));

        QueryCustomersResult result = queryCustomersUseCase.execute(query);
        QueryCustomersResponse response = QueryCustomersResponse.from(result);

        return ResponseEntity.ok(Map.of(
                "data", response.data(),
                "metadata", response.metadata()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable String id) {
        CustomerDetailResult result = getCustomerByIdUseCase.execute(new GetCustomerByIdQuery(Identifier.of(id)));
        GetCustomerByIdResponse response = GetCustomerByIdResponse.from(result);

        return ResponseEntity.ok(Map.of(
                "data", response.data(),
                "metadata", response.metadata()));
    }
}
