# Al Baraka Bank - Complete Testing Guide

A comprehensive guide to run, test, and verify all features of the Al Baraka Digital Banking Platform.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [API Testing (curl)](#api-testing-curl)
4. [Web Interface Testing](#web-interface-testing)
5. [Complete Test Scenarios](#complete-test-scenarios)

---

## Prerequisites

```bash
# Required
- Docker & Docker Compose
- Java 17+
- Maven 3.6+
- curl (for API testing)
```

---

## Quick Start

### Option 1: Docker Compose (Recommended)

```bash
# 1. Configure environment
cp .env.example .env
# Edit .env with your values

# 2. Start all services
docker-compose up --build

# 3. Access
# App: http://localhost:8080
# Keycloak: http://localhost:8180
```

### Option 2: Local Development

```bash
# 1. Start PostgreSQL
docker run -d --name albaraka-db \
  -e POSTGRES_DB=albaraka_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 postgres:15-alpine

# 2. Set environment variables
export DB_URL=jdbc:postgresql://localhost:5433/albaraka_db
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=$(echo "your-secret-key-at-least-32-chars" | base64)
export KEYCLOAK_ENABLED=false
export SPRING_AI_OPENAI_API_KEY=sk-your-key

# 3. Run application
./mvnw spring-boot:run -DskipTests

# 4. Verify
curl http://localhost:8080/login
```

---

## API Testing (curl)

### 1. Authentication

#### Register Client
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```
Expected: `{"token": "eyJ..."}`

#### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```
Expected: `{"token": "eyJ..."}`

Save token:
```bash
export CLIENT_TOKEN="eyJ..."
```

---

### 2. Client Operations

#### Create Deposit ≤ 10,000 DH (Auto-approved)
```bash
curl -X POST http://localhost:8080/api/client/operations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{"type": "DEPOSIT", "amount": 5000}'
```
Expected: `status: "EXECUTED"`, balance updated

#### Create Deposit > 10,000 DH (Pending)
```bash
curl -X POST http://localhost:8080/api/client/operations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{"type": "DEPOSIT", "amount": 25000}'
```
Expected: `status: "PENDING"`, requires document upload

#### Upload Document
```bash
curl -X POST http://localhost:8080/api/client/operations/1/document \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -F "file=@/path/to/document.pdf"
```
Expected: AI analyzes and returns decision

#### List Operations
```bash
curl -X GET http://localhost:8080/api/client/operations \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```

#### Create Withdrawal (with balance check)
```bash
curl -X POST http://localhost:8080/api/client/operations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{"type": "WITHDRAWAL", "amount": 1000}'
```

#### Create Transfer
```bash
curl -X POST http://localhost:8080/api/client/operations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{
    "type": "TRANSFER",
    "amount": 500,
    "destinationAccountNumber": "ALB123456789012"
  }'
```

---

### 3. Create Agent/Admin Users

```bash
# First register a user to get valid BCrypt hash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Temp","email":"temp@test.com","password":"password123"}'

# Get the hash and create agent/admin via SQL
docker exec albaraka-db psql -U postgres -d albaraka_db -c "
  INSERT INTO users (email, password, full_name, role, active, created_at) 
  SELECT 'agent@bank.com', password, 'Bank Agent', 'AGENT_BANCAIRE', true, NOW()
  FROM users WHERE email='temp@test.com';"

docker exec albaraka-db psql -U postgres -d albaraka_db -c "
  INSERT INTO users (email, password, full_name, role, active, created_at) 
  SELECT 'admin@bank.com', password, 'Admin User', 'ADMIN', true, NOW()
  FROM users WHERE email='temp@test.com';"
```

Get tokens:
```bash
export AGENT_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"agent@bank.com","password":"password123"}' | grep -oP '"token":"\K[^"]+')

export ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bank.com","password":"password123"}' | grep -oP '"token":"\K[^"]+')
```

---

### 4. Agent Operations

#### List Pending Operations
```bash
curl -X GET http://localhost:8080/api/agent/operations/pending \
  -H "Authorization: Bearer $AGENT_TOKEN"
```

#### Approve Operation
```bash
curl -X PUT http://localhost:8080/api/agent/operations/1/approve \
  -H "Authorization: Bearer $AGENT_TOKEN"
```
Expected: `status: "EXECUTED"`, balance updated

#### Reject Operation
```bash
curl -X PUT http://localhost:8080/api/agent/operations/2/reject \
  -H "Authorization: Bearer $AGENT_TOKEN"
```
Expected: `status: "CANCELLED"`, balance unchanged

---

### 5. Admin Operations

#### List All Users
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Create User
```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "fullName": "New Agent",
    "email": "newagent@bank.com",
    "password": "pass123",
    "role": "AGENT_BANCAIRE",
    "active": true
  }'
```

#### Update User
```bash
curl -X PUT http://localhost:8080/api/admin/users/5 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "fullName": "Updated Name",
    "email": "newagent@bank.com",
    "role": "AGENT_BANCAIRE",
    "active": true
  }'
```

#### Activate/Deactivate User
```bash
curl -X PUT http://localhost:8080/api/admin/users/5/activate \
  -H "Authorization: Bearer $ADMIN_TOKEN"

curl -X PUT http://localhost:8080/api/admin/users/5/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Delete User
```bash
curl -X DELETE http://localhost:8080/api/admin/users/5 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Web Interface Testing

### Pages to Test

| URL | Role | Description |
|-----|------|-------------|
| `/login` | Public | Login form with remember-me |
| `/register` | Public | Client registration form |
| `/dashboard` | Authenticated | Role-based dashboard |
| `/client/operations` | CLIENT | View/create operations |
| `/client/operations/{id}/upload` | CLIENT | Document upload |
| `/agent/operations` | AGENT | Pending operations list |
| `/admin/users` | ADMIN | User management |

### Web Test Flow

1. **Registration Flow**
   - Navigate to http://localhost:8080/register
   - Fill: Name, Email, Password, Confirm Password
   - Submit → Redirect to login with success message

2. **Login Flow**
   - Navigate to http://localhost:8080/login
   - Enter credentials, check "Remember me"
   - Submit → Redirect to dashboard

3. **Client Operations**
   - Click "View Operations" on dashboard
   - Create new operation (select type, enter amount)
   - For >10k operations, upload document

4. **Agent Approval**
   - Login as agent
   - View pending operations
   - Click Approve/Reject buttons

5. **Admin Management**
   - Login as admin
   - View user list
   - Create/activate/deactivate users

---

## Complete Test Scenarios

### Scenario 1: Client Registration & Account Creation ✅
```
1. POST /auth/register with valid data
2. Verify JWT returned
3. Check database: user created with role=CLIENT
4. Check database: account created with ALB prefix
```

### Scenario 2: Auto-Approved Deposit ✅
```
1. Login as client
2. POST /api/client/operations with amount=5000
3. Verify status=EXECUTED
4. Check balance increased by 5000
```

### Scenario 3: Pending Deposit with AI Analysis ✅
```
1. Login as client
2. POST /api/client/operations with amount=25000
3. Verify status=PENDING
4. POST document to /api/client/operations/{id}/document
5. AI returns: APPROVE/REJECT/NEED_HUMAN_REVIEW
6. If APPROVE: status=EXECUTED, balance updated
7. If REJECT: status=CANCELLED
8. If NEED_HUMAN_REVIEW: stays PENDING for agent
```

### Scenario 4: Withdrawal with Balance Check ✅
```
1. Login as client (has 5000 balance)
2. POST /api/client/operations with type=WITHDRAWAL, amount=10000
3. Verify error: "Insufficient balance"
4. POST with amount=1000
5. Verify status=EXECUTED, balance decreased
```

### Scenario 5: Transfer Between Accounts ✅
```
1. Login as client A
2. Get client B's account number
3. POST /api/client/operations with type=TRANSFER
4. Verify client A balance decreased
5. Verify client B balance increased
```

### Scenario 6: Agent Approval Workflow ✅
```
1. Client creates pending operation (>10k)
2. Login as agent
3. GET /api/agent/operations/pending
4. PUT /api/agent/operations/{id}/approve
5. Verify status=EXECUTED, balance updated
```

### Scenario 7: Agent Rejection ✅
```
1. Client creates pending operation
2. Login as agent
3. PUT /api/agent/operations/{id}/reject
4. Verify status=CANCELLED, balance unchanged
```

### Scenario 8: Admin User Management ✅
```
1. Login as admin
2. POST /api/admin/users (create agent)
3. GET /api/admin/users (list all)
4. PUT /api/admin/users/{id} (update)
5. PUT /api/admin/users/{id}/deactivate
6. DELETE /api/admin/users/{id}
```

---

## Run Automated Tests

```bash
# Unit + Integration tests
./mvnw test

# Expected: 8 tests pass
# - AuthControllerIntegrationTest
# - ClientOperationIntegrationTest
# - AgentOperationIntegrationTest
# - TransactionValidatorTest
# - DepositStrategyTest
# - WithdrawalStrategyTest
# - TransferStrategyTest
```

---

## Troubleshooting

### Database Connection Failed
```bash
# Check container
docker ps | grep albaraka-db
docker logs albaraka-db

# Restart
docker restart albaraka-db
```

### JWT Invalid
```bash
# Ensure JWT_SECRET is set and base64-encoded
echo "your-32-char-secret-key-here!!!" | base64
```

### AI Service Failed
```bash
# Check OpenAI key
echo $SPRING_AI_OPENAI_API_KEY

# AI errors default to NEED_HUMAN_REVIEW (graceful fallback)
```
