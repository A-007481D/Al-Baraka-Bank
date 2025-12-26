.PHONY: help build test run run-keycloak docker-up docker-down clean

# Default target
help:
	@echo "╔══════════════════════════════════════════════════════════════╗"
	@echo "║          Al Baraka Digital - Available Commands              ║"
	@echo "╚══════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Development:"
	@echo "  make build          - Build the application"
	@echo "  make test           - Run all tests"
	@echo "  make run            - Run app (without Keycloak)"
	@echo "  make run-keycloak   - Run app (with Keycloak enabled)"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-up      - Start all services (PostgreSQL + Keycloak + App)"
	@echo "  make docker-down    - Stop all services"
	@echo "  make docker-db      - Start only PostgreSQL"
	@echo ""
	@echo "Testing:"
	@echo "  make test-e2e       - Run end-to-end API tests"
	@echo ""
	@echo "Cleanup:"
	@echo "  make clean          - Clean build artifacts"

# Build
build:
	./mvnw clean package -DskipTests

# Run tests
test:
	./mvnw test

# Run locally (without Keycloak)
run:
	@echo "Starting Al Baraka Digital (Keycloak disabled)..."
	@echo "Make sure PostgreSQL is running on localhost:5432"
	DB_URL=jdbc:postgresql://localhost:5432/albaraka_db \
	DB_USER=postgres \
	DB_PASSWORD=postgres \
	JWT_SECRET=YWxiYXJha2EtYmFuay1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW5z \
	KEYCLOAK_ENABLED=false \
	./mvnw spring-boot:run

# Run locally (with Keycloak)
run-keycloak:
	@echo "Starting Al Baraka Digital (Keycloak enabled)..."
	@echo "Make sure PostgreSQL is on localhost:5432 and Keycloak on localhost:8180"
	DB_URL=jdbc:postgresql://localhost:5432/albaraka_db \
	DB_USER=postgres \
	DB_PASSWORD=postgres \
	JWT_SECRET=YWxiYXJha2EtYmFuay1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW5z \
	KEYCLOAK_ENABLED=true \
	KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/albaraka \
	./mvnw spring-boot:run

# Docker - start only PostgreSQL
docker-db:
	docker run -d --name albaraka-db \
		-e POSTGRES_DB=albaraka_db \
		-e POSTGRES_USER=postgres \
		-e POSTGRES_PASSWORD=postgres \
		-p 5432:5432 \
		postgres:15-alpine
	@echo "PostgreSQL started on port 5432"

# Docker - start all services
docker-up:
	docker-compose up -d
	@echo "Services starting..."
	@echo "  - PostgreSQL: localhost:5432"
	@echo "  - Keycloak:   localhost:8180"
	@echo "  - Backend:    localhost:8080"

# Docker - stop all services
docker-down:
	docker-compose down
	docker rm -f albaraka-db albaraka-keycloak 2>/dev/null || true

# End-to-end API tests
test-e2e:
	@echo "Running E2E tests..."
	@echo "1. Register user..."
	@curl -s -X POST http://localhost:8080/auth/register \
		-H "Content-Type: application/json" \
		-d '{"fullName":"Test User","email":"test@e2e.com","password":"password123"}' | jq '.'
	@echo ""
	@echo "2. Login..."
	@curl -s -X POST http://localhost:8080/auth/login \
		-H "Content-Type: application/json" \
		-d '{"email":"test@e2e.com","password":"password123"}' | jq '.'

# Clean
clean:
	./mvnw clean
	rm -rf target/
