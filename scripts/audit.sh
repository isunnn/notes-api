#!/bin/bash
set -e
ERRORS=0

echo "Auditoria de Seguridad y Calidad"

echo ""
echo "1. Buscando secrets hardcodeados en src/..."
if grep -rn "password\s*=\s*\"" src/ --include="*.java" --include="*.properties" --include="*.yml" 2>/dev/null | grep -v "test\|Test"; then
  echo "Posibles passwords hardcodeados encontrados"
  ERRORS=$((ERRORS + 1))
fi
if grep -rn "api[_-]key\s*=\s*\"" src/ --include="*.java" --include="*.properties" --include="*.yml" 2>/dev/null | grep -v "test\|Test"; then
  echo "Posibles API keys hardcodeadas encontradas"
  ERRORS=$((ERRORS + 1))
fi

echo ""
echo "2. Verificando Dockerfile..."
if grep -q "USER root" Dockerfile; then
  echo "Dockerfile ejecuta como root"
  ERRORS=$((ERRORS + 1))
else
  echo "Dockerfile no usa root"
fi

echo ""
echo "3. Verificando archivos sensibles..."
if git ls-files | grep -q "\.env$"; then
  echo "Archivo .env encontrado en el repositorio"
  ERRORS=$((ERRORS + 1))
else
  echo "No hay archivos .env commiteados"
fi

echo ""
echo "4. Verificando tags de imagenes en Dockerfile..."
if grep -qE "FROM.*:latest" Dockerfile; then
  echo "Dockerfile usa tag latest"
  ERRORS=$((ERRORS + 1))
else
  echo "Dockerfile usa tags especificos"
fi

echo ""
echo "Resultado de auditoria"
if [ $ERRORS -gt 0 ]; then
  echo "Auditoria FAILED con $ERRORS error(es)"
  exit 1
fi
echo "Auditoria PASSED"