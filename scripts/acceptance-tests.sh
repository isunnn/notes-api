#!/bin/bash
set -e

echo "Pruebas de aceptación"
API_URL=$1

if [ -z "$API_URL" ]; then
  echo "Uso: ./scripts/acceptance-tests.sh <URL_API>"
  exit 1
fi

echo "Test 1: Health check"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $API_URL/actuator/health)
if [ "$HTTP_CODE" != "200" ]; then
  echo "FAIL: Health check retornó $HTTP_CODE"
  exit 1
fi
echo "PASS"

echo "Test 2: GET /api/notas"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $API_URL/api/notas)
if [ "$HTTP_CODE" != "200" ]; then
  echo "FAIL: GET /api/notas retornó $HTTP_CODE"
  exit 1
fi
echo "PASS"

echo "Test 3: POST /api/notas"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $API_URL/api/notas \
  -H "Content-Type: application/json" \
  -d '{"nombreNota":"Test de aceptación"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
if [ "$HTTP_CODE" != "201" ]; then
  echo "FAIL: POST /api/notas retornó $HTTP_CODE"
  exit 1
fi
echo "PASS"

echo "Test 4: Verificar nota creada"
BODY=$(curl -s $API_URL/api/notas)
if echo "$BODY" | grep -q "Test de aceptación"; then
  echo "PASS"
else
  echo "FAIL: La nota no se creó correctamente"
  exit 1
fi

echo "Todas las pruebas pasaron"