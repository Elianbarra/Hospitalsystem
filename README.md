# Sistema Hospitalario — Microservicios

Sistema de gestión hospitalaria construido con arquitectura de microservicios en Java 25 y Spring Boot 3.5.3, desplegado en Kubernetes sobre Docker Desktop.

---

## Arquitectura general

```
Internet
    │
    ▼
[ Frontend ]  (Next.js — puerto 3001)
    │  fetch() desde el navegador (cliente)
    ▼ HTTP + JWT
[ BFF ]  ← único punto de entrada externo (puerto 8090)
    │    Valida JWT vía JWKS antes de rutear
    │    Orquesta TODA la comunicación entre microservicios
    │
    ├──▶ [ ms-auth       :8080 ]  Autenticación — emite JWT con RSA
    ├──▶ [ ms-user       :8081 ]  Gestión de usuarios
    ├──▶ [ ms-appointment:8082 ]  Gestión de citas médicas
    └──▶ [ ms-waitlist   :8083 ]  Lista de espera
```

> **Regla de arquitectura:** Los microservicios NO se comunican entre sí directamente. Toda orquestación pasa por el BFF. La única excepción es ms-user → ms-auth para registrar credenciales al momento de crear un usuario.

Todos los microservicios son **ClusterIP** (solo accesibles internamente en K8s). El BFF es el único que recibe tráfico externo.

---

## Flujo de comunicación Frontend → BFF

El frontend es una aplicación Next.js con componentes marcados como `"use client"`, lo que significa que **todas las peticiones HTTP se ejecutan desde el navegador del usuario**, no desde el servidor de Next.js.

La URL base del BFF se define en `src/lib/bff.ts` y se inyecta vía variable de entorno `NEXT_PUBLIC_BFF_URL`.

### ¿Quién hace las peticiones?

| Componente | Petición | Endpoint BFF |
|---|---|---|
| `LoginForm.tsx` | POST | `/api/auth/login` |
| `register/page.tsx` | POST | `/api/users` |
| `register/staff/page.tsx` | POST | `/api/users` |
| `dashboard/page.tsx` | GET | `/api/users/{userId}` |
| `dashboard/page.tsx` | GET | `/api/appointments/patient/{id}` |
| `dashboard/page.tsx` | GET | `/api/appointments/doctor/{id}` |
| `dashboard/page.tsx` | GET | `/api/users/specialty/{specialty}` |
| `dashboard/page.tsx` | GET | `/api/waitlist/specialty/{specialty}` |
| `dashboard/page.tsx` | POST | `/api/appointments` |
| `dashboard/page.tsx` | POST | `/api/waitlist` |
| `dashboard/page.tsx` | PUT | `/api/waitlist/{id}` |
| `dashboard/page.tsx` | PUT | `/api/waitlist/{id}/cancel` |
| `dashboard/page.tsx` | PUT | `/api/reasignacion/cancel-doctor/{id}` |
| `dashboard/page.tsx` | PUT | `/api/reasignacion/cancel-patient/{id}` |

### Flujo de autenticación

```
1. Usuario ingresa credenciales en LoginForm
2. Browser → POST /api/auth/login → BFF → ms-auth
3. ms-auth valida credenciales, genera JWT RSA, devuelve token
4. BFF reenvía la respuesta al browser
5. Browser almacena token en localStorage o sessionStorage
6. Todas las peticiones siguientes incluyen: Authorization: Bearer <token>
```

### Flujo de registro de paciente

```
1. Paciente completa formulario en register/page.tsx
2. Browser → POST /api/users → BFF → ms-user
3. ms-user guarda el usuario en su base de datos
4. ms-user → POST /api/auth/register → ms-auth (crea credenciales)
5. Redirect al login con ?registered=1
```

### Flujo de reasignación (cancelación por médico)

```
1. Médico cancela cita → PUT /api/reasignacion/cancel-doctor/{id} → BFF
2. BFF → ms-appointment: cancela la cita (cancelledBy=DOCTOR)
3. BFF → ms-waitlist: busca el siguiente paciente en cola para esa especialidad
4. BFF → ms-appointment: crea nueva cita para el siguiente paciente
5. BFF → ms-waitlist: marca la entrada del paciente como OFFERED
6. BFF responde con resultado compuesto (cita cancelada + nueva cita + entrada waitlist)
```

### Flujo de cancelación por paciente

```
1. Paciente cancela cita → PUT /api/reasignacion/cancel-patient/{id} → BFF
2. BFF → ms-appointment: cancela la cita (cancelledBy=PATIENT)
3. BFF → ms-waitlist: mueve al paciente al final de la cola (requeuedAt=now)
4. El paciente pierde la cita pero conserva su lugar en lista de espera
```

---

## Seguridad — Defensa en profundidad

```
Frontend → [JWT] → BFF ──valida JWT vía JWKS──▶ ms-auth (/.well-known/jwks.json)
                    │
                    └──[JWT propagado]──▶ ms-user / ms-appointment / ms-waitlist
                                              │
                                              └── cada uno re-valida el JWT
                                                  independientemente vía JWKS
```

El token JWT es emitido por **ms-auth** con un par de claves RSA `.pem`. La clave pública se expone en `/.well-known/jwks.json`. Cada microservicio descarga esa clave **una sola vez** y valida los tokens localmente.

Endpoints públicos (sin JWT): `POST /api/auth/login` y `POST /api/users` (registro de pacientes).

---

## Arquitectura

### Estilo arquitectónico

El sistema utiliza una **arquitectura de microservicios**, donde cada servicio tiene responsabilidad única, base de datos propia y se despliega de forma independiente.

```
┌─────────────┐    ┌──────────────────────────────────────────────┐
│  Frontend   │    │                 Kubernetes (namespace: hospital)│
│  (Next.js)  │    │  ┌─────────┐  ┌────────┐  ┌──────────────┐  │
│             │───▶│  │   BFF   │─▶│ms-auth │  │   ms-user    │  │
│  Browser    │    │  │ :8090   │─▶│ :8080  │  │    :8081     │  │
│  fetch()    │    │  │LoadBal. │  │ClusterIP│  │  ClusterIP   │  │
└─────────────┘    │  └─────────┘  └────────┘  └──────────────┘  │
                   │       │       ┌─────────────┐  ┌──────────┐  │
                   │       └──────▶│ms-appointment│  │ms-waitlist│ │
                   │               │    :8082     │  │   :8083  │  │
                   │               │  ClusterIP   │  │ClusterIP │  │
                   │               └─────────────┘  └──────────┘  │
                   └──────────────────────────────────────────────┘
```

### Capas internas de cada microservicio

Cada microservicio sigue una arquitectura en capas estricta:

```
Controller  →  Service  →  Repository  →  Base de datos
     ↕              ↕
    DTO           Entity
```

- **Controller**: recibe la petición HTTP, valida con `@Valid`, delega al Service.
- **Service**: contiene la lógica de negocio, orquesta el Repository y los clients externos.
- **Repository**: acceso a datos vía Spring Data JPA.
- **DTO**: objetos de transferencia de datos separados de las entidades JPA.
- **Entity**: mapeo a tabla de base de datos, nunca expuesto directamente al exterior.

---

## Patrones de diseño

### Backend For Frontend (BFF)
El `_BFF` actúa como único punto de entrada para el frontend. No expone los microservicios directamente: adapta, orquesta y agrega respuestas de múltiples servicios en una sola llamada. Ejemplo: el endpoint de reasignación coordina ms-appointment y ms-waitlist en un solo flujo transaccional desde el punto de vista del cliente.

### Facade
Aplicado en `AuthService` de ms-auth (documentado explícitamente en el código). Oculta la complejidad de BCrypt, JWT asimétrico con RSA y el repositorio detrás de métodos simples (`login`, `registerCredential`, `validateToken`) que el Controller consume sin conocer los detalles internos.

### Repository
Todos los microservicios usan Spring Data JPA con interfaces que extienden `JpaRepository`. La capa de acceso a datos queda completamente abstraída: el Service no conoce SQL, solo trabaja con métodos del repositorio.

### DTO (Data Transfer Object)
Cada microservicio define sus propios DTOs en `dto/request/` y `dto/response/`, separando el contrato de la API de la entidad JPA. Los nombres siguen la convención `XxxRequestDTO` / `XxxResponseDTO`. El BFF define sus equivalentes en `model/request/` y `model/response/` (pendiente de homologar a la misma convención).

### Builder
Utilizado extensivamente con la anotación `@Builder` de Lombok en entidades y DTOs. Permite construcción legible y segura de objetos complejos sin constructores con múltiples parámetros.

### Strategy
En `WaitlistService` el ordenamiento de la cola está encapsulado en un `Comparator<WaitlistEntry>` estático (`QUEUE_ORDER`) que implementa la estrategia de priorización: `vitalRisk=true` primero, luego `CRITICO > URGENTE > NORMAL`, luego por antigüedad (`requeuedAt ASC`). Cambiar la estrategia de priorización implica modificar solo ese comparador.

### Orchestrator
`ReasignacionService` en el BFF implementa el patrón de orquestador: coordina múltiples microservicios (ms-appointment, ms-waitlist) en una secuencia definida de pasos para completar un flujo de negocio complejo, sin que los microservicios individuales se conozcan entre sí.

### Soft Delete
Todas las entidades JPA incluyen un campo `active` (boolean). El borrado físico no se realiza: se marca `active = false` y las consultas filtran por `activeTrue`. Esto preserva el historial y permite auditoría.

---

## Buenas prácticas

### Separación de responsabilidades
Cada capa tiene una responsabilidad única y no invade la siguiente. Los Controllers no tienen lógica de negocio, los Services no construyen respuestas HTTP, los Repositories no contienen reglas de negocio.

### Validación en el punto de entrada
Todas las peticiones se validan con anotaciones Jakarta Validation (`@NotBlank`, `@Email`, `@Size`, `@FutureOrPresent`) sobre los DTOs de entrada, activadas con `@Valid` en el Controller. Los errores de validación son capturados por el `GlobalExceptionHandler`.

### Manejo global de excepciones
Cada microservicio y el BFF tienen un `GlobalExceptionHandler` con `@ControllerAdvice` que centraliza el manejo de errores y produce respuestas HTTP consistentes, evitando que las excepciones lleguen sin transformar al cliente.

### Logging estructurado
Se usa SLF4J con `@Slf4j` de Lombok en todas las capas de servicio. Los logs registran operaciones clave (registro de usuarios, login, cancelación de citas) con información contextual (IDs, especialidades, roles) para facilitar el diagnóstico.

### Configuración por entorno
Toda configuración sensible o dependiente del entorno se inyecta vía variables de entorno, sin valores hardcodeados en el código fuente. En Kubernetes se usa ConfigMap para configuración no sensible y Secret para credenciales.

### Stateless
La autenticación es completamente stateless: no hay sesiones en servidor. El JWT contiene toda la información necesaria (`userId`, `email`, `role`) y se valida localmente en cada servicio usando la clave pública JWKS.

### Database per Service
Cada microservicio tiene su propia base de datos PostgreSQL (NeonDB serverless). Ningún microservicio accede directamente a la base de datos de otro, garantizando desacoplamiento real entre servicios.

### Documentación de API automática
Todos los microservicios y el BFF usan Springdoc OpenAPI con anotaciones `@Operation` y `@Tag`. La documentación Swagger UI se genera automáticamente y está disponible en `/swagger-ui.html` de cada servicio.

### Autorización en frontend por rol
El frontend verifica el rol del usuario (`ADMIN`, `DOCTOR`, `PATIENT`) almacenado en sesión antes de renderizar vistas o hacer peticiones protegidas. La página de registro de personal valida que el usuario sea `ADMIN` antes de mostrar el formulario.

### Constantes centralizadas en frontend
Los valores reutilizables (roles, tipos de documento, etiquetas, clases CSS) están centralizados en archivos como `users.constants.ts` y `styles.ts`, evitando duplicación en los componentes.

---

## Microservicios

| Servicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| ms-auth | 8080 | Autenticación, emisión y validación de JWT RSA | NeonDB (PostgreSQL) |
| ms-user | 8081 | CRUD de usuarios (pacientes, médicos, administrativos) | NeonDB (PostgreSQL) |
| ms-appointment | 8082 | Gestión de citas médicas | NeonDB (PostgreSQL) |
| ms-waitlist | 8083 | Lista de espera con estrategias de prioridad | NeonDB (PostgreSQL) |
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
| Frontend | Next.js + Turbopack |
| CSS | Tailwind CSS |
| Tests backend | JUnit 5 + Mockito |
| Tests E2E | Playwright |

---

## Estructura del repositorio

```
hospital-system/
├── ms-auth/            # Microservicio de autenticación
├── ms-user/            # Microservicio de usuarios
├── ms-appointment/     # Microservicio de citas
├── ms-waitlist/        # Microservicio de lista de espera
├── bff/                # Backend For Frontend (orquestador)
├── frontend/           # Aplicación web (Next.js)
│   ├── src/
│   │   ├── app/        # Páginas y componentes Next.js
│   │   ├── features/   # Constantes y lógica por dominio
│   │   └── lib/        # Utilidades compartidas (bff.ts, session.ts)
│   └── tests/          # Tests E2E con Playwright
├── docs/
│   └── COMANDOS_DEMO.md
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
kubectl port-forward svc/bff-service 8090:8090 -n hospital
```

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Disponible en `http://localhost:3001`

---

## Ejecutar tests

### Tests unitarios (backend)

```bash
cd ms-appointment
./gradlew test
```

### Tests E2E (Playwright)

> Requiere que el sistema esté levantado (backend en K8s + port-forward + frontend corriendo).

```bash
cd frontend
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
kubectl apply -f ms-auth/k8s/secret.yaml
kubectl apply -f ms-user/k8s/secret.yaml
kubectl apply -f ms-appointment/k8s/secret.yaml
kubectl apply -f ms-waitlist/k8s/secret.yaml

# Desplegar servicios
kubectl apply -f ms-auth/k8s/
kubectl apply -f ms-user/k8s/
kubectl apply -f ms-appointment/k8s/
kubectl apply -f ms-waitlist/k8s/
kubectl apply -f bff/k8s/

# Verificar pods
kubectl get pods -n hospital

# Acceder a Swagger UI de cada servicio
kubectl port-forward svc/ms-auth-service        8080:8080 -n hospital
kubectl port-forward svc/ms-user-service        8081:8081 -n hospital
kubectl port-forward svc/ms-appointment-service 8082:8082 -n hospital
kubectl port-forward svc/ms-waitlist-svc        8083:8083 -n hospital
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
| `MS_AUTH_URL` | URL de ms-auth (para JWKS y registro de credenciales) |

### ms-appointment
| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión PostgreSQL (NeonDB) |
| `DB_USER` | Usuario de base de datos |
| `DB_PASS` | Contraseña de base de datos |
| `MS_AUTH_URL` | URL de ms-auth (para JWKS) |
| `MS_USER_URL` | URL de ms-user (para validar pacientes y doctores) |

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

## Equipo

| Nombre | Servicio principal |
|---|---|
| Aaron Lorca | ms-appointment, BFF |
| Elian Barra | ms-auth, ms-user |
| Cesar Estay | ms-waitlist |

---

## Licencia

Proyecto académico — Duoc UC
