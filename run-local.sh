#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== NOVA-1 Trade Finance Demo - Local Development Start ===${NC}\n"

# Fix CRLF line endings if on Linux/Mac
if [ -f "guarantees-service/mvnw" ]; then
    echo "Fixing mvnw line endings..."
    sed -i 's/\r$//' "guarantees-service/mvnw"
fi

# Check if node_modules exists in frontend
if [ ! -d "guarantees-ui/node_modules" ]; then
    echo -e "${BLUE}Installing frontend dependencies...${NC}"
    cd guarantees-ui
    npm install
    cd ..
fi

# Start backend in background
echo -e "${GREEN}Starting backend (Spring Boot)...${NC}"
(cd guarantees-service && ./mvnw spring-boot:run) &
BACKEND_PID=$!

# Wait for backend to be ready
echo "Waiting for backend to be ready..."
sleep 5
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        echo -e "${GREEN}✓ Backend is ready${NC}\n"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ Backend failed to start${NC}"
        kill $BACKEND_PID
        exit 1
    fi
    sleep 1
done

# Start frontend
echo -e "${GREEN}Starting frontend (Angular)...${NC}"
(cd guarantees-ui && npm start) &
FRONTEND_PID=$!

sleep 3
echo -e "${GREEN}✓ Frontend is starting (check http://localhost:4200)${NC}\n"

echo -e "${BLUE}=== Demo is running ===${NC}"
echo -e "Frontend: ${BLUE}http://localhost:4200${NC}"
echo -e "Backend: ${BLUE}http://localhost:8080/api/v1/guarantees${NC}"
echo -e "Swagger: ${BLUE}http://localhost:8080/swagger-ui.html${NC}"
echo -e "H2 Console: ${BLUE}http://localhost:8080/h2-console${NC}\n"

# Trap exit signal to kill background processes
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT

# Wait for both processes
wait
