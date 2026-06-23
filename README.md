# Sistema Hospitalario — Microservicios

Sistema de gestión hospitalaria construido con arquitectura de microservicios en Java 25 y Spring Boot 3.5.3, desplegado en Kubernetes.

---

## Arquitectura general

```
Internet
    │
    ▼
[ Frontend ]  (React / Next.js — puerto 3001)
    │
    ▼ HTTPS
[ NAT Gateway / Ingress ]
    │
    ▼
[ BFF ]  ← único punto de entrada externo (puerto 8090)
    │    Valida JWT vía JWKS antes de rutear
    │
    ├──▶ [ ms-auth       :8080 ]  Autenticación — emite JWT con RSA
    ├──▶ [ ms-user       :8081 ]  Gestión de usuarios
    ├──▶ [ ms-appointment:8082 ]  Gestión de citas médicas
    └──▶ [ ms-waitlist   :8083 ]  Lista de espera
```

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

El token JWT es emitido por **ms-auth** con un par de claves RSA `.pem`. La clave pública se expone en `/.well-known/jwks.json`. Cada microservicio descarga esa clave **una sola vez** y valida los tokens localmente en adelante, sin depender de ms-auth en cada request.

---

## Microservicios

| Servicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| ms-auth | 8080 | Autenticación, emisión y validación de JWT RSA | PostgreSQL |
| ms-user | 8081 | CRUD de usuarios (pacientes, médicos, administrativos) | PostgreSQL |
| ms-appointment | 8082 | Gestión de citas médicas | PostgreSQL |
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
| Comunicación inter-servicio | Spring RestClient (nativo Spring Boot 3.2+) |
| Documentación API | Springdoc OpenAPI 2.8.8 (Swagger UI) |
| Base de datos | PostgreSQL 17 |
| Migraciones | Flyway 11.x |
| Build | Gradle 8+ con Kotlin DSL |
| Contenedores | Docker (eclipse-temurin:25 multi-stage) |
| Orquestación | Kubernetes (local: minikube / kind) |
| Escalado | HorizontalPodAutoscaler (HPA) |

---

## Estructura del repositorio

```
hospital-system/
├── ms-auth/            # Microservicio de autenticación
├── ms-user/            # Microservicio de usuarios
├── ms-appointment/     # Microservicio de citas
├── ms-waitlist/        # Microservicio de lista de espera
├── bff/                # Backend For Frontend
├── frontend/           # Aplicación web (React / Next.js)
└── README.md           # Este archivo
```

Cada microservicio contiene su propia carpeta `k8s/` con todos los manifiestos necesarios para desplegarlo.

---

## Prerrequisitos

- Java 25 LTS (`sdk install java 25-tem` con SDKMAN, o Homebrew `brew install --cask temurin@25`)
- Docker Desktop
- kubectl
- minikube o kind (para Kubernetes local)
- PostgreSQL 17 (o cuenta en [neon.tech](https://neon.tech) para cloud)

---

## Levantar en local (sin Kubernetes)

Cada servicio tiene un archivo `.env.example`. Copia y completa los valores:

```bash
cp ms-auth/.env.example        ms-auth/.env
cp ms-user/.env.example        ms-user/.env
cp ms-appointment/.env.example ms-appointment/.env
cp ms-waitlist/.env.example    ms-waitlist/.env
```

Luego en cada carpeta:

```bash
./gradlew bootRun
```

Orden recomendado de inicio: `ms-auth` → `ms-user` → `ms-appointment` → `ms-waitlist` → `bff`

---

## Despliegue en Kubernetes (local)

```bash
# 1. Crear namespace
kubectl apply -f ms-auth/k8s/namespace.yaml

# 2. Desplegar en orden (auth primero, luego el resto)
kubectl apply -f ms-auth/k8s/
kubectl apply -f ms-user/k8s/
kubectl apply -f ms-appointment/k8s/
kubectl apply -f ms-waitlist/k8s/
kubectl apply -f bff/k8s/

# 3. Verificar pods
kubectl get pods -n hospital

# 4. Acceder al BFF (port-forward mientras no hay Ingress)
kubectl port-forward svc/bff-svc 8090:8090 -n hospital
```

Para acceder a la Swagger UI de cada servicio en local:

```bash
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
| `DB_URL` | URL de conexión PostgreSQL |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `JWT_PRIVATE_KEY_PATH` | Ruta al archivo `.pem` de clave privada RSA |
| `JWT_PUBLIC_KEY_PATH` | Ruta al archivo `.pem` de clave pública RSA |

### ms-user
| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión PostgreSQL |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `MS_AUTH_URL` | URL de ms-auth (para JWKS y registro de credenciales) |

### ms-appointment
| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión PostgreSQL |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `MS_AUTH_URL` | URL de ms-auth (para JWKS) |
| `MS_USER_URL` | URL de ms-user (para validar paciente/médico) |

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

## Flujo de autenticación

```
1. Cliente → POST /api/auth/login { email, password }
         ← { token: "eyJ..." }

2. Cliente → GET /api/users/me
             Authorization: Bearer eyJ...
         → BFF valida token via JWKS
         → BFF reenvía request + Bearer a ms-user
         → ms-user re-valida token via JWKS
         ← { id, nombre, rol, ... }
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
