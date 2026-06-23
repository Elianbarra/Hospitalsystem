# Sistema Hospitalario — Microservicios

Sistema de gestión hospitalaria construido con arquitectura de microservicios en Java 25 y Spring Boot 3.5.3, desplegado en Kubernetes sobre Docker Desktop.

---

## Arquitectura general

```
Internet
    │
    ▼
[ Frontend ]  (Next.js 16 — puerto 3001)
    │
    ▼ HTTP
[ BFF ]  ← único punto de entrada externo (puerto 8090)
    │    Valida JWT vía JWKS antes de rutear
    │    Orquesta TODA la comunicación entre microservicios
    │
    ├──▶ [ ms-auth       :8080 ]  Autenticación — emite JWT con RSA
    ├──▶ [ ms-user       :8081 ]  Gestión de usuarios
    ├──▶ [ ms-appointment:8082 ]  Gestión de citas médicas
    └──▶ [ ms-waitlist   :8083 ]  Lista de espera (en memoria)
```

> **Regla de arquitectura:** Los microservicios NO se comunican entre sí directamente. Toda orquestación pasa por el BFF.

Todos los microservicios son **ClusterIP** (solo accesibles internamente en K8s). El BFF es el único que recibe tráfico externo.

### Seguridad — Defensa en profundidad

```
Frontend → [JWT] → BFF ──valida JWT vía JWKS──▶ ms-auth (/.well-known/jwks.json)
                    │
                    └──[JWT propagado]──▶ ms-user / ms-appointment / ms-waitlist
                                              │
                                              └── cada uno re-valida el JWT
                                                  independientemente vía JWKS
```

El token JWT es emitido por **ms-auth** con un par de claves RSA `.pem`. La clave pública se expone en `/.well-known/jwks.json`. Cada microservicio descarga esa clave **una sola vez** y valida los tokens localmente.

---

## Microservicios

| Servicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| ms-auth | 8080 | Autenticación, emisión y validación de JWT RSA | NeonDB (PostgreSQL) |
| ms-user | 8081 | CRUD de usuarios (pacientes, médicos, administrativos) | NeonDB (PostgreSQL) |
| ms-appointment | 8082 | Gestión de citas médicas | NeonDB (PostgreSQL) |
| ms-waitlist | 8083 | Lista de espera con estrategias de prioridad | En memoria |
| BFF | 8090 | Orquestador — valida JWT y rutea al ms correspondiente | — |

---

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 25 LTS |
| Framework | Spring Boot 3.5.3 |
| Seguridad | Spring Security + OAuth2 Resource Server |
| JWT | RSA Key Pair + JWKS (Nimbus / Spring Authorization Server) |
| Documentación API | Springdoc OpenAPI 2.8.8 (Swagger UI) |
| Base de datos | PostgreSQL 17 (NeonDB — serverless) |
| Migraciones | Flyway 11.x |
| Build | Gradle 8+ con Kotlin DSL |
| Contenedores | Docker (eclipse-temurin:25 multi-stage) |
| Orquestación | Kubernetes (Docker Desktop) |
| Frontend | Next.js 16 + Turbopack |
| Tests backend | JUnit 5 + Mockito |
| Tests E2E | Playwright |

---

## Estructura del repositorio

```
hospital-system/
├── Ms-Auth/            # Microservicio de autenticación
├── Ms-User/            # Microservicio de usuarios
├── Ms-appointment/     # Microservicio de citas
├── Ms-Waitlist/        # Microservicio de lista de espera
├── BFF/                # Backend For Frontend (orquestador)
├── Frontend/           # Aplicación web (Next.js)
│   └── tests/          # Tests E2E con Playwright
├── docs/
│   └── COMANDOS_DEMO.md  # Cheat sheet para la demo
└── README.md
```

---

## Prerrequisitos

- Java 25 LTS
- Docker Desktop (con Kubernetes habilitado)
- kubectl
- Node.js 20+

---

## Levantar el sistema

### 1. Backend (Kubernetes)

```bash
# Desde la raíz del proyecto
./deploy.sh
```

El script compila todos los microservicios, construye las imágenes Docker y las despliega en Kubernetes.

Verificar que todos los pods estén corriendo:

```bash
kubectl get pods -n hospital
```

Exponer el BFF (puerto 8090):

```bash
kubectl port-forward svc/bff-svc 8090:8090 -n hospital
```

### 2. Frontend

```bash
cd Frontend
npm install
npm run dev
```

Disponible en `http://localhost:3001`

---

## Ejecutar tests

### Tests unitarios (backend)

```bash
cd Ms-appointment
./gradlew test
```

### Tests E2E (Playwright)

> Requiere que el sistema esté levantado (backend en K8s + port-forward + frontend corriendo).

```bash
cd Frontend
npm run test:e2e
```

Ver reporte HTML:

```bash
npm run test:e2e:report
```

---

## Credenciales de demo

| Rol | Email | Contraseña |
|---|---|---|
| Administrador | admin@rednorte.cl | Admin2024! |
| Doctor (Cardiología) | doctor@rednorte.cl | Doctor2024! |

---

## Despliegue en Kubernetes — detalle

```bash
# Aplicar secrets (NO están en el repo — agregar manualmente)
kubectl apply -f Ms-Auth/k8s/secret.yaml
kubectl apply -f Ms-User/k8s/secret.yaml
kubectl apply -f Ms-appointment/k8s/secret.yaml
kubectl apply -f Ms-Waitlist/k8s/secret.yaml

# Desplegar servicios
kubectl apply -f Ms-Auth/k8s/
kubectl apply -f Ms-User/k8s/
kubectl apply -f Ms-appointment/k8s/
kubectl apply -f Ms-Waitlist/k8s/
kubectl apply -f BFF/k8s/

# Verificar pods
kubectl get pods -n hospital

# Acceder a Swagger UI de cada servicio
kubectl port-forward svc/ms-auth-svc        8080:8080 -n hospital
kubectl port-forward svc/ms-user-svc        8081:8081 -n hospital
kubectl port-forward svc/ms-appointment-svc 8082:8082 -n hospital
kubectl port-forward svc/ms-waitlist-svc    8083:8083 -n hospital
```

Swagger UI disponible en `http://localhost:<puerto>/swagger-ui.html`

---

## Variables de entorno por servicio

### ms-auth
| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión PostgreSQL (NeonDB) |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `JWT_PRIVATE_KEY_PATH` | Ruta al archivo `.pem` de clave privada RSA |
| `JWT_PUBLIC_KEY_PATH` | Ruta al archivo `.pem` de clave pública RSA |

### ms-user
| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión PostgreSQL (NeonDB) |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `MS_AUTH_URL` | URL de ms-auth (para JWKS) |

### ms-appointment
| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión PostgreSQL (NeonDB) |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `MS_AUTH_URL` | URL de ms-auth (para JWKS) |

### ms-waitlist
| Variable | Descripción |
|---|---|
| `MS_AUTH_URL` | URL de ms-auth (para JWKS) |

### BFF
| Variable | Descripción |
|---|---|
| `MS_AUTH_URL` | URL de ms-auth |
| `MS_USER_URL` | URL de ms-user |
| `MS_APPOINTMENTS_URL` | URL de ms-appointment |
| `MS_WAITLIST_URL` | URL de ms-waitlist |
| `FRONTEND_URL` | URL del frontend (para CORS) |

---

## Flujo principal — tomar una cita

```
1. Paciente registra cuenta → POST /api/users/register
2. Paciente hace login     → POST /api/auth/login  →  JWT
3. Paciente ve slots       → GET  /api/appointments/slots
4. Paciente toma hora      → POST /api/appointments
5. Doctor ve sus citas     → GET  /api/appointments/doctor/{id}
6. Doctor cancela hora     → PUT  /api/reasignacion/cancel-doctor/{id}
   └─▶ BFF cancela cita en ms-appointment
   └─▶ BFF busca siguiente en lista de espera (ms-waitlist)
   └─▶ BFF reasigna automáticamente
```

---

## Equipo

| Nombre | Servicio principal |
|---|---|
| Aaron Lorca | ms-appointment, BFF |
| Elian Barra | ms-auth, ms-user |
| Cesar Estay | ms-waitlist |

---

## Licencia

Proyecto académico — Duoc UC
