#!/bin/bash
# =============================================================================
# deploy.sh — Construye imágenes y despliega en Kubernetes (Docker Desktop)
#
# Uso:
#   chmod +x deploy.sh
#   ./deploy.sh
# =============================================================================

set -e

echo "🏥 Sistema Hospitalario — Deploy en Kubernetes"
echo "=============================================="

# ── 1. Compilar JARs localmente ───────────────────────────────────────────────
echo ""
echo "🔨 Compilando JARs con Gradle (usando Java 25 local)..."

for svc in ms-auth ms-user ms-appointment ms-waitlist bff; do
  echo "  → $svc"
  (cd "$svc" && ./gradlew bootJar -x test --no-daemon -q)
done

echo "✅ JARs compilados"

# ── 2. Construir imágenes Docker ──────────────────────────────────────────────
echo ""
echo "📦 Construyendo imágenes Docker..."

docker build -t ms-auth:latest        ./ms-auth
docker build -t ms-user:latest        ./ms-user
docker build -t ms-appointment:latest ./ms-appointment
docker build -t ms-waitlist:latest    ./ms-waitlist
docker build -t bff:latest            ./bff

echo "✅ Imágenes construidas"

# ── 2. Aplicar namespace primero ──────────────────────────────────────────────
echo ""
echo "🌐 Creando namespace..."
kubectl apply -f ms-auth/k8s/namespace.yaml

# ── 3. Aplicar secrets y configmaps ──────────────────────────────────────────
echo ""
echo "🔐 Aplicando secrets y configuración..."
kubectl apply -f ms-auth/k8s/secret.yaml

# Crear secret con las claves RSA de ms-auth (desde archivos locales)
kubectl create secret generic ms-auth-rsa-keys \
  --namespace hospital \
  --from-file=private_key.pem=ms-auth/volumes/ms-auth/keys/private_key.pem \
  --from-file=public_key.pem=ms-auth/volumes/ms-auth/keys/public_key.pem \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f ms-auth/k8s/configmap.yaml
kubectl apply -f ms-user/k8s/secret.yaml
kubectl apply -f ms-user/k8s/configmap.yaml
kubectl apply -f ms-appointment/k8s/secret.yaml
kubectl apply -f ms-appointment/k8s/configmap.yaml
kubectl apply -f ms-waitlist/k8s/secret.yaml
kubectl apply -f ms-waitlist/k8s/configmap.yaml
kubectl apply -f bff/k8s/configmap.yaml

# ── 4. Aplicar PVCs ──────────────────────────────────────────────────────────
echo ""
echo "💾 Aplicando volúmenes persistentes..."
kubectl apply -f ms-auth/k8s/postgres-pvc.yaml
kubectl apply -f ms-user/k8s/postgres-pvc.yaml
kubectl apply -f ms-appointment/k8s/postgres-pvc.yaml

# ── 5. Aplicar bases de datos ─────────────────────────────────────────────────
echo ""
echo "🗄️  Desplegando bases de datos..."
kubectl apply -f ms-auth/k8s/postgres-service.yaml
kubectl apply -f ms-auth/k8s/postgres-deployment.yaml
kubectl apply -f ms-user/k8s/postgres-service.yaml
kubectl apply -f ms-user/k8s/postgres-deployment.yaml
kubectl apply -f ms-appointment/k8s/postgres-service.yaml
kubectl apply -f ms-appointment/k8s/postgres-deployment.yaml

echo "⏳ Esperando que las bases de datos estén listas..."
kubectl wait --namespace hospital \
  --for=condition=ready pod \
  --selector=tier=database \
  --timeout=120s

# ── 6. Aplicar microservicios ─────────────────────────────────────────────────
echo ""
echo "🚀 Desplegando microservicios..."
kubectl apply -f ms-auth/k8s/service.yaml
kubectl apply -f ms-auth/k8s/deployment.yaml

echo "⏳ Esperando ms-auth..."
kubectl wait --namespace hospital \
  --for=condition=ready pod \
  --selector=app=ms-auth \
  --timeout=180s

kubectl apply -f ms-user/k8s/service.yaml
kubectl apply -f ms-user/k8s/deployment.yaml
kubectl apply -f ms-appointment/k8s/service.yaml
kubectl apply -f ms-appointment/k8s/deployment.yaml
kubectl apply -f ms-waitlist/k8s/deployment.yaml

# ── 7. Aplicar bff ───────────────────────────────────────────────────────────
echo ""
echo "🔀 Desplegando bff..."
kubectl apply -f bff/k8s/configmap.yaml
kubectl apply -f bff/k8s/deployment.yaml
kubectl apply -f bff/k8s/service.yaml

# ── 8. Resumen ────────────────────────────────────────────────────────────────
echo ""
echo "✅ Deploy completado"
echo ""
echo "Estado de los pods:"
kubectl get pods -n hospital
echo ""
echo "Servicios expuestos:"
kubectl get services -n hospital
echo ""
echo "El bff estará disponible en: http://localhost:3000"
echo "El frontend (si está corriendo): http://localhost:3001"
