# Al Baraka Digital - Testing Guide

## Quick Start

### Prerequisites
- Java 21+
- Docker
- Maven (or use included `./mvnw`)

### Run Tests
```bash
# Unit + Integration tests
make test
# or
./mvnw test
```

---

## Running the Application

### Option 1: Without Keycloak (Simple JWT Auth)

```bash
# 1. Start PostgreSQL
make docker-db
# or
docker run -d --name albaraka-db \
  -e POSTGRES_DB=albaraka_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:15-alpine

# 2. Run application
make run
# or
DB_URL=jdbc:postgresql://localhost:5432/albaraka_db \
DB_USER=postgres DB_PASSWORD=postgres \
JWT_SECRET=YWxiYXJha2EtYmFuay1zZWNyZXQta2V5 \
KEYCLOAK_ENABLED=false \
./mvnw spring-boot:run
```

### Option 2: With Keycloak (OAuth2 + JWT)

```bash
# 1. Start all services
make docker-up
# or
docker-compose up -d

# 2. Wait for services to start (1-2 minutes)
# 3. Access:
#    - API: http://localhost:8080
#    - Keycloak: http://localhost:8180 (admin/admin)
```

---

## API Testing

### 1. Register Client
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"client@test.com","password":"pass123"}'
```

### 2. Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"client@test.com","password":"pass123"}' | jq -r '.token')
```

### 3. Create Deposit
```bash
# Auto-approved (≤10,000 DH)
curl -X POST http://localhost:8080/api/client/operations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"DEPOSIT","amount":5000}'

# Pending (>10,000 DH)
curl -X POST http://localhost:8080/api/client/operations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"DEPOSIT","amount":15000}'
```

### 4. Create Withdrawal
```bash
curl -X POST http://localhost:8080/api/client/operations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"WITHDRAWAL","amount":2000}'
```

### 5. Create Transfer
```bash
curl -X POST http://localhost:8080/api/client/operations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"TRANSFER","amount":1000,"destinationAccountNumber":"ACCOUNT_NUMBER"}'
```

### 6. List Operations
```bash
curl http://localhost:8080/api/client/operations \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Upload Document
```bash
curl -X POST http://localhost:8080/api/client/operations/1/document \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@document.pdf"
```

---

## Agent Endpoints

```bash
# Create agent (via database)
docker exec albaraka-db psql -U postgres -d albaraka_db -c \
  "INSERT INTO users (email, password, full_name, role, active, created_at) 
   VALUES ('agent@bank.com', '<bcrypt_hash>', 'Agent', 'AGENT_BANCAIRE', true, NOW());"

# Login as agent
AGENT_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"agent@bank.com","password":"pass123"}' | jq -r '.token')

# Get pending operations
curl http://localhost:8080/api/agent/operations/pending \
  -H "Authorization: Bearer $AGENT_TOKEN"

# Approve operation
curl -X PUT http://localhost:8080/api/agent/operations/1/approve \
  -H "Authorization: Bearer $AGENT_TOKEN"

# Reject operation
curl -X PUT http://localhost:8080/api/agent/operations/1/reject \
  -H "Authorization: Bearer $AGENT_TOKEN"
```

---

## Admin Endpoints

```bash
# Create admin user, login, then:

# Create user
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"New User","email":"new@bank.com","password":"pass","role":"CLIENT","active":true}'

# List users
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Deactivate user
curl -X PUT http://localhost:8080/api/admin/users/1/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Delete user
curl -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Keycloak Testing

### Setup Keycloak Realm
1. Go to http://localhost:8180
2. Login with admin/admin
3. Create realm "albaraka"
4. Create client "albaraka-bank-api" (public, direct access grants enabled)
5. Create roles: client, agent, admin
6. Create users and assign roles

### Get Keycloak Token
```bash
KC_TOKEN=$(curl -s -X POST http://localhost:8180/realms/albaraka/protocol/openid-connect/token \
  -d "client_id=albaraka-bank-api" \
  -d "username=user@test.com" \
  -d "password=password" \
  -d "grant_type=password" | jq -r '.access_token')
```

### Use Keycloak Endpoints
```bash
curl http://localhost:8080/api/keycloak/client/operations \
  -H "Authorization: Bearer $KC_TOKEN"
```

---

## Cleanup

```bash
make docker-down
# or
docker-compose down
docker rm -f albaraka-db albaraka-keycloak
```
