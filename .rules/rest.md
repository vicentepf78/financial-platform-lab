# REST API Guidelines

## HTTP methods

GET, POST, PUT, PATCH, DELETE — follow REST conventions.

## Response envelope

When applicable:

```json
{
  "data": {},
  "metadata": {}
}
```

## Errors

Use Problem Details (RFC 9457 style).

## Controllers

Controllers adapt HTTP → DTO and delegate to use cases. No business rules in controllers.
