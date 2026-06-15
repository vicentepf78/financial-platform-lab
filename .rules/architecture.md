# Architecture Rules

## Primary Architecture

Hexagonal Architecture combined with DDD Light, Vertical Slice Architecture, and Modular Monolith.

## Allowed Dependencies

```text
Controller  -> UseCase
UseCase       -> Domain
UseCase       -> Ports
Adapters      -> Ports
Infrastructure -> Adapters
```

## Forbidden Dependencies

```text
Domain -> Controller
Domain -> Repository Implementation
Domain -> Spring
Domain -> Kafka
Domain -> REST
```

## Module Structure

```text
module/
  domain/
  application/
  ports/
  adapters/
  infrastructure/
```

## Feature Structure

Every business capability in one vertical slice:

```text
features/create-account/
  CreateAccountController.java
  CreateAccountUseCase.java
  CreateAccountRequest.java
  CreateAccountResponse.java
  CreateAccountUseCaseTest.java
```

Never spread a feature across unrelated folders.

## Golden Rules (architecture)

**Rule 1 — Business rules location:** Only in Domain and Application (use cases). Never in controllers, REST endpoints, Kafka consumers, or repositories.

**Rule 4 — No framework in domain:** Domain must not depend on Spring, JPA, Kafka, or HTTP.

**Rule 5:** Favor composition over inheritance.

**Rule 6:** Max ~200 lines per class, ~30 lines per method.

**Rule 7:** Favor immutability.
