# Al Baraka Digital Banking Platform

Secure banking platform with JWT authentication, role-based access control, and automated transaction workflows.

## Features

- JWT stateless authentication
- Role-based access (CLIENT, AGENT_BANCAIRE, ADMIN)
- Automated transaction approval (10,000 DH threshold)
- Deposit, withdrawal, and transfer operations
- Document upload for high-value transactions
- Agent validation workflow
- Admin user management

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 15+ (or use Docker Compose)
- Docker & Docker Compose (for containerized deployment)

## Quick Start

### 1. Environment Setup

Copy the example environment file and configure your secrets:

```bash
cp .env.example .env
```

Edit `.env` and set your own secure values:

```env
POSTGRES_DB=albaraka_db
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_secure_db_password
JWT_SECRET=your_jwt_secret_key_here_must_be_base64_encoded
UPLOAD_DIR=/app/uploads
```

> **Security Note**: Never commit the `.env` file to version control. It's already in `.gitignore`.

### 2. Run with Docker Compose (Recommended)

```bash
docker-compose up --build
```

The application will be available at `http://localhost:8080`

### 3. Run Locally (Development)

Ensure PostgreSQL is running, then:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/albaraka_db
export DB_USER=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret

./mvnw spring-boot:run
```

## API Documentation

### Authentication

#### Register (Client)
```bash
POST /auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

#### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

Response: `{"token": "eyJhbGciOiJ..."}`

### Client Operations

All client endpoints require `Authorization: Bearer <token>` header.

#### Create Operation
```bash
POST /api/client/operations
Authorization: Bearer <token>

{
  "type": "DEPOSIT",
  "amount": 5000
}
```

Types: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`

For transfers, include:
```json
{
  "type": "TRANSFER",
  "amount": 5000,
  "destinationAccountNumber": "ALB123456789"
}
```

#### Upload Document (for operations > 10,000 DH)
```bash
POST /api/client/operations/{id}/document
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <PDF/JPG/PNG file, max 5MB>
```

#### List Operations
```bash
GET /api/client/operations
Authorization: Bearer <token>
```

### Agent Operations

#### List Pending Operations
```bash
GET /api/agent/operations/pending
Authorization: Bearer <token>
```

#### Approve Operation
```bash
PUT /api/agent/operations/{id}/approve
Authorization: Bearer <token>
```

#### Reject Operation
```bash
PUT /api/agent/operations/{id}/reject
Authorization: Bearer <token>
```

### Admin Operations

#### Create User
```bash
POST /api/admin/users
Authorization: Bearer <token>

{
  "fullName": "Jane Agent",
  "email": "agent@albaraka.com",
  "password": "password123",
  "role": "AGENT_BANCAIRE",
  "active": true
}
```

Roles: `CLIENT`, `AGENT_BANCAIRE`, `ADMIN`

#### List All Users
```bash
GET /api/admin/users
Authorization: Bearer <token>
```

## Business Rules

### Automatic Approval Threshold
- Operations ≤ 10,000 DH: Auto-approved, balance updated immediately
- Operations > 10,000 DH: Status = PENDING, requires document upload and agent approval

### Operation Types
- **DEPOSIT**: Credits the account balance
- **WITHDRAWAL**: Debits the account (requires sufficient balance)
- **TRANSFER**: Debits source account, credits destination account

## Project Structure

```
src/main/java/com/albaraka_bank/
├── common/              # Utilities, exception handling
├── config/              # Security, JWT configuration
├── modules/
│   ├── iam/            # Identity & Access Management
│   │   ├── controller/ # Auth, Admin endpoints
│   │   ├── dto/        # Request/Response objects
│   │   ├── model/      # User entity
│   │   └── service/    # Auth, JWT services
│   ├── account/        # Account management
│   │   ├── model/      # Account entity
│   │   ├── repository/
│   │   └── service/    # Account operations
│   └── operation/      # Banking operations
│       ├── controller/ # Client, Agent endpoints
│       ├── dto/
│       ├── model/      # Operation, Document entities
│       ├── repository/
│       └── service/    # Business logic
```

## Security

- **JWT**: Stateless authentication with 24-hour token expiration
- **BCrypt**: Password hashing
- **Role-based Access**: Spring Security method-level authorization
- **Environment Variables**: Secrets managed via `.env` file (not committed)

## Development

### Build
```bash
./mvnw clean compile
```

### Test
```bash
./mvnw test
```

### Package
```bash
./mvnw clean package
```

## Deployment

### Docker

Build the image:
```bash
docker build -t albaraka-bank .
```

Run with environment variables:
```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/albaraka_db \
  -e DB_USER=user \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=secret \
  albaraka-bank
```

### Production Considerations

1. **JWT Secret**: Generate a strong, random Base64-encoded secret
2. **Database**: Use managed PostgreSQL service with backups
3. **File Storage**: Consider cloud storage (S3, Azure Blob) for documents
4. **HTTPS**: Deploy behind reverse proxy with TLS termination
5. **Monitoring**: Add logging, metrics, and health checks

## License

Proprietary - Al Baraka Bank
