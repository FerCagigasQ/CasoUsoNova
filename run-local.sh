#!/usr/bin/env bash
set -e

echo "=== Verificando prerequisitos ==="
command -v java >/dev/null 2>&1 || { echo "❌ Java 17 requerido. Instalar: https://adoptium.net"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "❌ Node.js 18+ requerido. Instalar: https://nodejs.org"; exit 1; }

echo "✓ Java $(java -version 2>&1 | grep -i version | head -1 || echo '(versión desconocida)')"
echo "✓ Node.js $(node --version)"
echo ""

BACKEND_PID=""
FRONTEND_PID=""

cleanup() {
  echo ""
  echo "=== Deteniendo servicios ==="
  if [ -n "$BACKEND_PID" ]; then
    kill $BACKEND_PID 2>/dev/null || true
  fi
  if [ -n "$FRONTEND_PID" ]; then
    kill $FRONTEND_PID 2>/dev/null || true
  fi
  wait $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "=== Arrancando backend (Spring Boot) ==="
cd guarantees-service
./mvnw spring-boot:run > /tmp/guarantees-backend.log 2>&1 &
BACKEND_PID=$!
cd ..

echo "Esperando backend (máx 60s)..."
for i in $(seq 1 60); do
  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✓ Backend listo"
    break
  fi
  if [ $i -eq 60 ]; then
    echo "❌ Backend no respondió en 60s"
    cat /tmp/guarantees-backend.log
    exit 1
  fi
  sleep 1
done

echo ""
echo "=== Arrancando frontend (Angular) ==="
cd guarantees-ui
npm install > /tmp/guarantees-frontend-install.log 2>&1
npm start > /tmp/guarantees-frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

echo "Esperando frontend (máx 30s)..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:4200 > /dev/null 2>&1; then
    echo "✓ Frontend listo"
    break
  fi
  if [ $i -eq 30 ]; then
    echo "⚠ Frontend puede estar compilando, continuando..."
    break
  fi
  sleep 1
done

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                   🏦 DEMO LISTA 🏦                             ║"
echo "╠════════════════════════════════════════════════════════════════╣"
echo "║  Frontend:      http://localhost:4200                          ║"
echo "║  API REST:      http://localhost:8080/api/v1/guarantees        ║"
echo "║  Swagger UI:    http://localhost:8080/swagger-ui.html          ║"
echo "║  H2 Console:    http://localhost:8080/h2-console              ║"
echo "║                                                                ║"
echo "║  Logs:                                                         ║"
echo "║    Backend:  tail -f /tmp/guarantees-backend.log              ║"
echo "║    Frontend: tail -f /tmp/guarantees-frontend.log             ║"
echo "║                                                                ║"
echo "║  Ctrl+C para detener todo                                      ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

wait
