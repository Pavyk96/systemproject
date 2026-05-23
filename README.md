# System Project

Spring Boot application for basic project and task management with role-based access, server-side HTML pages on Thymeleaf, PostgreSQL persistence, and Swagger/OpenAPI documentation.

## Documentation

- Application pages and user scenarios: [docs/pages.md](docs/pages.md)
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## Run locally

1. Start PostgreSQL:
   ```bash
   docker compose up -d postgres
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Open the app:
   - `http://localhost:8080/login`

Default bootstrap admin credentials are configured in `src/main/resources/application.yaml`:

- username: `admin`
- password: `admin`
