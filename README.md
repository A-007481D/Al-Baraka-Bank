# Al Baraka Digital Banking Platform

[![CI/CD Pipeline](https://github.com/YOUR-USERNAME/albaraka-bank/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/YOUR-USERNAME/albaraka-bank/actions/workflows/ci-cd.yml)
[![Code Quality](https://github.com/YOUR-USERNAME/albaraka-bank/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/YOUR-USERNAME/albaraka-bank/actions/workflows/qodana_code_quality.yml)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue.svg)](https://spring.io/projects/spring-ai)

Secure banking platform with **dual JWT/OAuth2 authentication**, **AI-powered risk analysis**, role-based access control, and automated transaction workflows.

---

## Table of Contents

- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Authentication & Security](#authentication--security)
  - [JWT Stateless Authentication](#jwt-stateless-authentication)
  - [OAuth2/Keycloak Integration](#oauth2keycloak-integration)
  - [Security Filter Chains](#security-filter-chains)
- [Business Logic](#business-logic)
  - [Banking Operations](#banking-operations)
  - [Automatic vs Manual Validation](#automatic-vs-manual-validation)
  - [Strategy Pattern Implementation](#strategy-pattern-implementation)
- [Spring AI Integration](#spring-ai-integration)
- [REST API Documentation](#rest-api-documentation)
- [Docker & Deployment](#docker--deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Project Structure](#project-structure)
- [Development](#development)
- [Performance Criteria](#performance-criteria)

---

## Features

- ✅ **Dual Authentication**: JWT stateless tokens + OAuth2/Keycloak
- ✅ **Role-based Access Control**: CLIENT, AGENT_BANCAIRE, ADMIN
- ✅ **AI-Powered Risk Analysis**: Automated transaction validation using Spring AI
- ✅ **Automated Workflows**: 10,000 DH threshold for auto-approval
- ✅ **Banking Operations**: Deposit, Withdrawal, Transfer
- ✅ **Document Upload**: Justification for high-value transactions
- ✅ **Agent Validation**: Manual review workflow with audit trail
- ✅ **Admin Management**: User CRUD operations
- ✅ **Web Interface**: Thymeleaf-based dashboard with Bootstrap 5
- ✅ **Docker Ready**: Multi-service deployment with Docker Compose
- ✅ **CI/CD**: Automated testing and deployment via GitHub Actions

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Al Baraka Digital Platform                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Client    │  │    Agent    │  │    Admin    │  │   External Apps     │ │
│  │  (Browser)  │  │  (Browser)  │  │  (Browser)  │  │   (API Consumers)   │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘ │
│         │                │                │                    │            │
│         └────────────────┴────────────────┴────────────────────┘            │
│                                    │                                        │
│  ┌─────────────────────────────────▼─────────────────────────────────────┐  │
│  │                     Spring Security Filter Chains                     │  │
│  │  ┌──────────────┐  ┌──────────────────┐  ┌────────────────────────┐   │  │
│  │  │ Web (Order 1)│  │ Keycloak (Order 2)│ │    API JWT (Order 3)   │   │  │
│  │  │ Form Login   │  │ OAuth2 Resource   │ │    Bearer Token        │   │  │
│  │  │ Remember-Me  │  │ Server            │ │    Stateless           │   │  │
│  │  └──────────────┘  └──────────────────┘  └────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                        │
│  ┌─────────────────────────────────▼─────────────────────────────────────┐  │
│  │                          Application Layer                            │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │     IAM     │  │   Account   │  │  Operation  │  │     AI      │   │  │
│  │  │   Module    │  │   Module    │  │   Module    │  │   Module    │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                        │
│  ┌─────────────────────────────────▼─────────────────────────────────────┐  │
│  │                          Data Layer (JPA)                             │  │ 
│  │              PostgreSQL Database + File Storage                       │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Authentication & Security

### JWT Stateless Authentication

The platform implements **stateless JWT authentication** for REST API endpoints.

```
┌──────────┐         ┌──────────────┐          ┌─────────────┐
│  Client  │         │ AuthController│         │  JwtService │
└────┬─────┘         └───────┬───────┘         └──────┬──────┘
     │                       │                        │
     │  POST /auth/login     │                        │
     │  {email, password}    │                        │
     │──────────────────────>│                        │
     │                       │                        │
     │                       │  authenticate()        │
     │                       │───────────────────────>│
     │                       │                        │
     │                       │  generateToken(user)   │
     │                       │<───────────────────────│
     │                       │                        │
     │  200 {"token": "..."} │                        │
     │<──────────────────────│                        │
     │                       │                        │
     │  GET /api/client/ops  │                        │
     │  Authorization: Bearer xyz                     │
     │──────────────────────>│                        │
     │                       │                        │
     │           ┌───────────▼───────────┐            │
     │           │      JwtFilter        │            │
     │           │  - Extract token      │            │
     │           │  - Validate signature │            │
     │           │  - Load UserDetails   │            │
     │           │  - Set SecurityContext│            │
     │           └───────────────────────┘            │
```

**Key Configuration:**
| Parameter | Value | Description |
|-----------|-------|-------------|
| Algorithm | HS256 | HMAC-SHA256 signing |
| Expiration | 24 hours | Token validity |
| Password Encoding | BCrypt | Industry-standard hashing |

**JWT Flow:**
1. Client sends credentials to `/auth/login`
2. Server validates and returns signed JWT
3. Client includes token in `Authorization: Bearer <token>` header
4. `JwtFilter` validates token on each request
5. User context set in `SecurityContextHolder`

### OAuth2/Keycloak Integration

For enterprise deployments, the platform supports **Keycloak as an OAuth2 Resource Server**.

```yaml
# application.yaml
keycloak:
  enabled: ${KEYCLOAK_ENABLED:false}

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/albaraka}
```

**Keycloak Realm Configuration:**
- Realm: `albaraka`
- Roles: `client`, `agent`, `admin`
- Client: `albaraka-bank-api` (public client, Direct Grant enabled)
- Token Lifespan: 24 hours

### Security Filter Chains

The application uses **three ordered security filter chains**:

| Order | Matcher | Authentication | Session |
|-------|---------|----------------|---------|
| 1 | `/`, `/login`, `/dashboard`, `/agent/**`, `/admin/**` | Form Login + Remember-Me | Stateful |
| 2 | `/api/keycloak/**` | OAuth2 Resource Server | Stateless |
| 3 | `/api/**`, `/auth/**` | JWT Bearer Token | Stateless |

```java
// SecurityConfig.java - Three filter chains
@Order(1) webSecurityFilterChain     // Web UI with form login
@Order(2) keycloakSecurityFilterChain // OAuth2 for Keycloak tokens
@Order(3) apiSecurityFilterChain      // JWT for REST API
```

---

## Business Logic

### Banking Operations

| Operation | Description | Balance Effect |
|-----------|-------------|----------------|
| `DEPOSIT` | Credit funds to account | +Amount |
| `WITHDRAWAL` | Debit funds from account | -Amount |
| `TRANSFER` | Move funds between accounts | Source: -Amount, Dest: +Amount |

### Automatic vs Manual Validation

```
                    ┌─────────────────────┐
                    │ Create Operation    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Amount > 10,000 DH? │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              │ NO                              │ YES
              ▼                                 ▼
    ┌─────────────────┐              ┌─────────────────────┐
    │ Auto-Approved   │              │ Status: PENDING     │
    │ Status: EXECUTED│              │ Require Document    │
    │ Balance Updated │              └──────────┬──────────┘
    └─────────────────┘                         │
                                                ▼
                                    ┌─────────────────────┐
                                    │ Upload Justificatif │
                                    │ (PDF/JPG/PNG, 5MB)  │
                                    └──────────┬──────────┘
                                                │
                                                ▼
                                    ┌─────────────────────┐
                                    │   AI Risk Analysis  │
                                    │   (Spring AI)       │
                                    └──────────┬──────────┘
                                                │
                      ┌─────────────────────────┼─────────────────────────┐
                      │ APPROVE                 │ NEED_HUMAN_REVIEW       │ REJECT
                      ▼                         ▼                         ▼
            ┌─────────────────┐     ┌─────────────────────┐    ┌─────────────────┐
            │ Auto-Execute    │     │ Agent Review Queue  │    │ Auto-Reject     │
            │ Balance Updated │     │ Manual Decision     │    │ No Balance Chg  │
            └─────────────────┘     └─────────────────────┘    └─────────────────┘
```

### Strategy Pattern Implementation

Banking operations use the **Strategy Pattern** for clean separation of concerns:

```
OperationFactory
      │
      ├── DepositStrategy.process(amount, source, dest)
      │       └── source.balance += amount
      │
      ├── WithdrawalStrategy.process(amount, source, dest)
      │       └── source.balance -= amount
      │
      └── TransferStrategy.process(amount, source, dest)
              ├── source.balance -= amount
              └── dest.balance += amount
```

---

## Spring AI Integration

The platform uses **Spring AI with OpenAI** for intelligent transaction risk analysis.

### AI Service Configuration

```yaml
spring:
  ai:
    openai:
      api-key: ${SPRING_AI_OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
```

### AI Decision Types

| Decision | Description | Action |
|----------|-------------|--------|
| `APPROVE` | Low-risk transaction | Auto-execute operation |
| `REJECT` | Suspicious transaction | Auto-reject, notify client |
| `NEED_HUMAN_REVIEW` | Uncertain risk | Queue for agent review |

### AI Risk Rules

```java
// AiService.java prompt logic
Rules:
1. If amount < 20,000 DH and document is 'payslip' → APPROVE
2. If amount > 50,000 DH → NEED_HUMAN_REVIEW
3. If document is 'suspicious' → REJECT
4. Otherwise → NEED_HUMAN_REVIEW
```

---

## REST API Documentation

### Authentication Endpoints

#### Register (Client)
```http
POST /auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:** `200 OK`
```json
{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

### Client Endpoints

> All endpoints require `Authorization: Bearer <token>` with `CLIENT` role.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/client/operations` | Create operation |
| `GET` | `/api/client/operations` | List user operations |
| `POST` | `/api/client/operations/{id}/document` | Upload document |

#### Create Operation
```http
POST /api/client/operations
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "DEPOSIT",
  "amount": 5000
}
```

**Types:** `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`

For transfers:
```json
{
  "type": "TRANSFER",
  "amount": 5000,
  "destinationAccountNumber": "ALB123456789"
}
```

#### Upload Document
```http
POST /api/client/operations/{id}/document
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <PDF/JPG/PNG, max 5MB>
```

### Agent Endpoints

> Requires `AGENT_BANCAIRE` role.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/agent/operations/pending` | List pending operations |
| `PUT` | `/api/agent/operations/{id}/approve` | Approve operation |
| `PUT` | `/api/agent/operations/{id}/reject` | Reject operation |

### Admin Endpoints

> Requires `ADMIN` role.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/users` | List all users |
| `POST` | `/api/admin/users` | Create user |
| `PUT` | `/api/admin/users/{id}` | Update user |
| `DELETE` | `/api/admin/users/{id}` | Delete user |

#### Create User
```http
POST /api/admin/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "Jane Agent",
  "email": "agent@albaraka.com",
  "password": "password123",
  "role": "AGENT_BANCAIRE",
  "active": true
}
```

**Roles:** `CLIENT`, `AGENT_BANCAIRE`, `ADMIN`

### OAuth2/Keycloak Endpoints

When Keycloak is enabled, parallel endpoints are available:

| Endpoint | Description |
|----------|-------------|
| `/api/keycloak/client/**` | Client operations (Keycloak auth) |
| `/api/keycloak/agent/**` | Agent operations (Keycloak auth) |
| `/api/keycloak/admin/**` | Admin operations (Keycloak auth) |

### Error Responses

| Status | Description |
|--------|-------------|
| `400 Bad Request` | Validation error, insufficient balance |
| `401 Unauthorized` | Invalid/missing token |
| `403 Forbidden` | Insufficient permissions |
| `404 Not Found` | Resource not found |
| `500 Internal Server Error` | Server error |

---

## Docker & Deployment

### Dockerfile (Multi-stage Build)

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Docker Compose Services

```yaml
services:
  postgres:      # PostgreSQL 15 database
  keycloak:      # Keycloak 23.0 identity server
  backend:       # Spring Boot application
```

### Quick Start

```bash
# 1. Configure environment
cp .env.example .env
# Edit .env with your secrets

# 2. Start all services
docker-compose up --build

# 3. Access
# - App: http://localhost:8080
# - Keycloak: http://localhost:8180
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | - |
| `POSTGRES_USER` | Database user | - |
| `POSTGRES_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing key (Base64) | - |
| `UPLOAD_DIR` | File upload directory | `/app/uploads` |
| `KEYCLOAK_ENABLED` | Enable Keycloak | `false` |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm URL | - |
| `SPRING_AI_OPENAI_API_KEY` | OpenAI API key | - |

---

## CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, "feature/**"]
  pull_request:
    branches: [main]

jobs:
  test:
    # Run Maven tests with JDK 17
    
  build-and-push:
    # Build Docker image
    # Push to GitHub Container Registry (ghcr.io)
```

### Pipeline Stages

```
┌────────────────┐     ┌────────────────┐     ┌────────────────┐
│   Checkout     │────>│  Run Tests     │────>│  Build JAR     │
└────────────────┘     └────────────────┘     └────────────────┘
                                                      │
┌────────────────┐     ┌────────────────┐            │
│  Push to GHCR  │<────│  Build Docker  │<───────────┘
└────────────────┘     └────────────────┘
```

### Image Tags

| Tag | Description |
|-----|-------------|
| `latest` | Latest main branch build |
| `<sha>` | Commit-specific build |

---

## Project Structure

```
src/main/java/com/albaraka_bank/
├── AlbarakaBankApplication.java    # Main entry point
├── common/                         # Utilities, exception handling
├── config/                         # Security, JWT configuration
│   ├── JwtFilter.java             # JWT authentication filter
│   ├── KeycloakJwtConverter.java  # OAuth2 role converter
│   └── SecurityConfig.java        # 3 security filter chains
├── modules/
│   ├── iam/                       # Identity & Access Management
│   │   ├── controller/            # Auth, Admin endpoints
│   │   ├── dto/                   # Request/Response objects
│   │   ├── model/                 # User entity
│   │   └── service/               # Auth, JWT services
│   ├── account/                   # Account management
│   │   ├── model/                 # Account entity
│   │   ├── repository/
│   │   └── service/               # Account operations
│   ├── operation/                 # Banking operations
│   │   ├── controller/            # Client, Agent, Keycloak endpoints
│   │   ├── dto/
│   │   ├── factory/               # Strategy factory
│   │   ├── model/                 # Operation, Document entities
│   │   ├── repository/
│   │   ├── service/               # Business logic
│   │   └── strategy/              # Deposit, Withdrawal, Transfer
│   └── ai/                        # Spring AI integration
│       ├── model/                 # AiDecision enum
│       └── service/               # AiService (OpenAI)
└── web/                           # Thymeleaf controllers
```

---

## Development

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 15+ (or Docker)
- Docker & Docker Compose

### Build & Test

```bash
# Compile
./mvnw clean compile

# Run tests
./mvnw test

# Package
./mvnw clean package

# Run locally
export DB_URL=jdbc:postgresql://localhost:5432/albaraka_db
export DB_USER=your_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_base64_secret
./mvnw spring-boot:run
```

---

## Performance Criteria

### Business Rules Compliance ✅
- 10,000 DH threshold strictly enforced
- Balance validation before withdrawal/transfer
- Document upload required for high-value transactions
- Audit trail for all operations

### JWT Stateless Implementation ✅
- No server-side session storage
- 24-hour token expiration
- HS256 signature verification
- BCrypt password hashing

### Endpoint Security ✅
- Role-based access (CLIENT, AGENT_BANCAIRE, ADMIN)
- Three distinct security filter chains
- OAuth2 Resource Server for Keycloak
- Method-level security with `@PreAuthorize`

### Code Architecture ✅
- Clean separation: Controller → Service → Repository
- Strategy Pattern for operations
- Factory Pattern for strategy selection
- Modular package structure

### Spring AI Integration ✅
- OpenAI GPT-3.5-turbo for risk analysis
- Prompt-based decision engine
- Graceful fallback to human review

### CI/CD Pipeline ✅
- Automated testing on push
- Docker image build and push
- GitHub Container Registry integration

### Docker Deployment ✅
- Multi-stage build for optimized images
- Docker Compose orchestration
- PostgreSQL + Keycloak + Backend services

---

## License

Proprietary - Al Baraka Bank
