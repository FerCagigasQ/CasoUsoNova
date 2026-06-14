#!/usr/bin/env bash
set -e

echo "=== Verificando prerequisitos ==="
command -v java >/dev/null 2>&1 || { echo "ERROR: Java 17 requerido"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "ERROR: Node.js 18+ requerido"; exit 1; }

echo "=== Arrancando backend ==="
cd guarantees-service
chmod +x mvnw 2>/dev/null || true
sed -i 's/\r$//' mvnw 2>/dev/null || true
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

echo "=== Esperando backend (max 90s) ==="
for i in $(seq 1 90); do
  if curl -sf http://localhost:8080/api/v1/guarantees > /dev/null 2>&1; then
    echo "Backend listo!"
    break
  fi
  sleep 1
done

echo "=== Arrancando frontend ==="
cd guarantees-ui
npm install --silent
npm start &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================="
echo "  DEMO LISTA"
echo "========================================="
echo "  Frontend:   http://localhost:4200"
echo "  API:        http://localhost:8080/api/v1/guarantees"
echo "  Swagger:    http://localhost:8080/swagger-ui.html"
echo "  H2 Console: http://localhost:8080/h2-console"
echo ""
echo "  Ctrl+C para parar todo"
echo "========================================="

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT
wait
