# Al Baraka Bank - Automation Makefile

.PHONY: help all build up down restart logs clean backend-logs

help:
	@echo "Available commands:"
	@echo "  make all      - Full restart (Down -> Build -> Up)"
	@echo "  make build    - Build the backend JAR (skipping tests)"
	@echo "  make up       - Start containers (rebuilds Docker images)"
	@echo "  make down     - Stop and remove containers"
	@echo "  make restart  - Same as 'make all'"
	@echo "  make logs     - View logs for all services"
	@echo "  make backend-logs - View logs for backend only"

all: restart

build:
	mvn clean package -DskipTests

up:
	docker-compose up -d --build

down:
	docker-compose down --remove-orphans

restart: down build up
	@echo "🚀 Application is starting up..."
	@echo "⏳ Waiting for backend to initialize..."
	@sleep 10
	@docker-compose logs --tail=10 backend

logs:
	docker-compose logs -f

backend-logs:
	docker-compose logs -f backend
