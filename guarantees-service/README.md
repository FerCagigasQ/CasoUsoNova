# Guarantees Service

A complete REST API for managing guarantees with Spring Boot 3.2.x, Maven, and Java 17.

## Features

- **Guarantee Management**: Full CRUD operations for guarantees
- **Applicants & Beneficiaries**: Manage participants in guarantee relationships
- **Issuing Banks**: Track banks issuing guarantees
- **Amendments**: Record changes to guarantee terms
- **Claims**: Submit and track guarantee claims
- **Swagger UI**: Interactive API documentation at `/swagger-ui.html`
- **H2 Database**: In-memory database with seed data

## Prerequisites

- Java 17+
- Maven 3.8+ (or use the included Maven Wrapper)

## Building

```bash
# Using Maven Wrapper (recommended)
./mvnw clean package -DskipTests

# Or with system Maven
mvn clean package -DskipTests
```

## Running

```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/guarantees-service-1.0.0.jar
```

The application starts on `http://localhost:8080`

## API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console (use `sa` as username, no password)

## API Endpoints

### Guarantees
- `GET /api/v1/guarantees` - List all guarantees
- `GET /api/v1/guarantees/{id}` - Get guarantee by ID
- `POST /api/v1/guarantees` - Create new guarantee
- `PUT /api/v1/guarantees/{id}` - Update guarantee
- `DELETE /api/v1/guarantees/{id}` - Delete guarantee
- `GET /api/v1/guarantees/status/{status}` - Filter by status
- `POST /api/v1/guarantees/{id}/issue` - Issue a guarantee
- `POST /api/v1/guarantees/{id}/amend` - Amend guarantee
- `POST /api/v1/guarantees/{id}/claim` - Submit claim
- `GET /api/v1/guarantees/{id}/amendments` - List amendments
- `GET /api/v1/guarantees/{id}/claims` - List claims

## Seed Data

The application initializes with:
- **3 Banks**: BBVA, Santander, CaixaBank
- **4 Applicants**: John Doe, Jane Smith, Carlos García, María López
- **4 Beneficiaries**: Alice Johnson, Bob Williams, Diego Martínez, Isabel Rodríguez
- **6 Guarantees** in various states: ACTIVE, EXPIRED, AMENDED, CLAIMED, ISSUED

## Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **ORM**: Hibernate JPA
- **API Documentation**: Springdoc-OpenAPI 2.3.0
- **Build**: Maven 3.9.5

## Docker

Build and run with Docker:

```bash
docker build -t guarantees-service:1.0.0 .
docker run -p 8080:8080 guarantees-service:1.0.0
```

## Project Structure

```
guarantees-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/guarantees/
│   │   │   ├── controller/          # REST endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access
│   │   │   ├── domain/              # JPA entities
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   └── config/              # Configuration classes
│   │   └── resources/
│   │       └── application.yml       # Configuration
│   └── test/
├── pom.xml                           # Maven configuration
├── mvnw & mvnw.bat                   # Maven Wrapper
└── Dockerfile                        # Multi-stage Docker build
```

## Entity Relationships

- **Guarantee**: References Applicant, Beneficiary, IssuingBank
- **Amendment**: References Guarantee (one-to-many)
- **Claim**: References Guarantee (one-to-many)

## Status Values

- **GuaranteeStatus**: ISSUED, ACTIVE, AMENDED, EXPIRED, CLAIMED, CANCELLED
- **ClaimStatus**: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PAID

## Error Handling

The API returns standard HTTP status codes:
- `200`: Success
- `201`: Created
- `204`: No Content
- `400`: Bad Request
- `404`: Not Found
- `500`: Internal Server Error

## Development

To run tests:

```bash
./mvnw test
```

To check code format:

```bash
./mvnw clean verify
```

## License

Apache 2.0
