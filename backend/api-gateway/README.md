# api-gateway

API Gateway del sistema hospitalario construido con **Spring Cloud Gateway**.

Es el único punto de entrada externo al cluster. Su responsabilidad es validar el JWT y enrutar los requests al BFF — no contiene lógica de negocio.

---

## Topología

```
Frontend (:3001)
    │
    ▼
api-gateway (:8091)  ← LoadBalancer (este servicio)
    │  valida JWT · gestiona CORS · enruta
    ▼
BFF (:8090)          ← ClusterIP (solo accesible internamente)
    │
    ├──▶ ms-auth       :8080
    ├──▶ ms-user       :8081
    ├──▶ ms-appointment:8082
    └──▶ ms-waitlist   :8083
```

---

## Rutas configuradas

| ID | Path | Requiere JWT | Destino |
|---|---|---|---|
| `auth-login` | `/api/auth/login`, `/api/auth/register` | No | BFF |
| `bff-protected` | `/api/**` | Sí | BFF |

---

## Levantar (primera vez o después de cambios)

```bash
# 1. Compilar
./gradlew bootJar

# 2. Construir imagen Docker
docker build -t api-gateway:latest .

# 3. Desplegar en Kubernetes
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 4. Verificar
kubectl get pods -n hospital
kubectl logs -n hospital -l app=api-gateway --tail=30
```

## Actualizar (después de cambios en el código)

```bash
./gradlew bootJar
docker build -t api-gateway:latest .
kubectl rollout restart deployment/api-gateway -n hospital
```

---

## Verificar que funciona

```bash
# 1. Health check (sin token)
curl http://localhost:8091/actuator/health
# Esperado: {"status":"UP"}

# 2. Ruta pública — debe llegar al BFF
curl -X POST http://localhost:8091/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@rednorte.cl","password":"Admin2024!"}'

# 3. Ruta protegida sin token — debe ser rechazada por el gateway
curl -v http://localhost:8091/api/users
# Esperado: 401 Unauthorized (con header WWW-Authenticate: Bearer)
```

---

## Variables de entorno

Definidas en `k8s/configmap.yaml`:

| Variable | Valor en K8s | Descripción |
|---|---|---|
| `MS_AUTH_URL` | `http://ms-auth-service:8080` | Para descargar JWKS y validar JWT |
| `BFF_URL` | `http://bff-service:8090` | Destino de todas las rutas |
| `FRONTEND_URL` | `http://localhost:3001` | Para CORS |

---

## Nota sobre compatibilidad

Spring Cloud `2024.0.x` soporta formalmente Spring Boot hasta `3.4.x`. Este proyecto usa Boot `3.5.3`, por lo que se deshabilita el verificador de compatibilidad en `application.yml`:

```yaml
spring:
  cloud:
    compatibility-verifier:
      enabled: false
```

Las librerías son funcionalmente compatibles — el check es solo de versión.
