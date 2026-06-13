# BFF — Salud RedNorte · Gestor de Citas Hospitalarias

**Stack:** Java 21 · Spring Boot 3.4 · Gradle 8 · MVC

---

## Arquitectura del sistema

```
Browser / App
     │
     ▼
┌─────────────────────────┐
│      frontend/          │  ← Next.js 16 · TypeScript · Tailwind   :3001
│  (repo independiente)   │     cd frontend && npm run dev
└──────────┬──────────────┘
           │  HTTP  (NEXT_PUBLIC_BFF_URL)
           ▼
┌─────────────────────────┐
│         BFF             │  ← Spring Boot 3.4 · Java 21  :8090  ← este proyecto
│  MVC puro — solo API    │
└──┬──────┬──────┬───┬────┘
   │      │      │   │
   ▼      ▼      ▼   ▼
 ms-    ms-   ms-ap  ms-
 auth   user  point  wait
 :8080  :8081  ment  list
               :8082  :8083
               (*)    (*)

(*) pendiente de implementación — el BFF ya tiene las rutas y modelos listos.
```

---

## Arquitectura MVC del BFF

```
src/main/java/cl/rednorte/bff/
├── BffApplication.java
│
├── config/
│   ├── CorsConfig.java          ← CORS: permite origen FRONTEND_URL
│   └── RestClientConfig.java    ← Beans RestClient por microservicio
│
├── controller/                  ← Capa C — recibe HTTP, valida, delega al Service
│   ├── AuthController.java
│   ├── UserController.java
│   ├── AppointmentController.java
│   └── WaitlistController.java
│
├── service/                     ← Capa S — orquesta llamadas HTTP a microservicios
│   ├── AuthService.java
│   ├── UserService.java
│   ├── AppointmentService.java
│   └── WaitlistService.java
│
├── model/                       ← Capa M — DTOs de entrada y salida (Java records)
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── CreateAppointmentRequest.java
│   │   ├── UpdateAppointmentRequest.java
│   │   └── CreateWaitlistEntryRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── TokenValidationResponse.java
│       ├── UserResponse.java
│       ├── AppointmentResponse.java
│       └── WaitlistEntryResponse.java
│
└── exception/
    ├── ApiException.java         ← Error tipado con status HTTP
    └── GlobalExceptionHandler.java ← @RestControllerAdvice centralizado
```

---

## API Routes del BFF

### Auth — ms-auth :8080
| Método | Ruta BFF             | Redirige a                        |
|--------|----------------------|-----------------------------------|
| POST   | /api/auth/login      | ms-auth POST /api/auth/login      |
| GET    | /api/auth/validate   | ms-auth GET  /api/auth/validate   |

### Users — ms-user :8081
| Método | Ruta BFF             | Redirige a                         |
|--------|----------------------|------------------------------------|
| GET    | /api/users           | ms-user GET  /api/users            |
| POST   | /api/users           | ms-user POST /api/users/register   |
| GET    | /api/users/{id}      | ms-user GET  /api/users/{id}       |
| PUT    | /api/users/{id}      | ms-user PUT  /api/users/{id}       |
| DELETE | /api/users/{id}      | ms-user DELETE /api/users/{id}     |

### Appointments — ms-appointment :8082 *(pendiente)*
| Método | Ruta BFF                              | Redirige a                              |
|--------|---------------------------------------|-----------------------------------------|
| GET    | /api/appointments                     | ms-appointment GET /api/appointments    |
| POST   | /api/appointments                     | ms-appointment POST /api/appointments   |
| GET    | /api/appointments/{id}                | ms-appointment GET /api/appointments/{id} |
| PUT    | /api/appointments/{id}                | ms-appointment PUT /api/appointments/{id} |
| DELETE | /api/appointments/{id}                | ms-appointment DELETE                   |
| GET    | /api/appointments/patient/{patientId} | ms-appointment GET /patient/{id}        |
| GET    | /api/appointments/doctor/{doctorId}   | ms-appointment GET /doctor/{id}         |

### Waitlist — ms-waitlist :8083 *(pendiente)*
| Método | Ruta BFF                            | Redirige a                            |
|--------|-------------------------------------|---------------------------------------|
| GET    | /api/waitlist                       | ms-waitlist GET /api/waitlist         |
| POST   | /api/waitlist                       | ms-waitlist POST /api/waitlist        |
| GET    | /api/waitlist/{id}                  | ms-waitlist GET /api/waitlist/{id}    |
| DELETE | /api/waitlist/{id}                  | ms-waitlist DELETE                    |
| GET    | /api/waitlist/patient/{patientId}   | ms-waitlist GET /patient/{id}         |

---

## Variables de entorno

```properties
# application.properties / variables del entorno de deploy

MS_AUTH_URL=http://localhost:8080
MS_USER_URL=http://localhost:8081
MS_APPOINTMENTS_URL=http://localhost:8082   # descomentar cuando esté disponible
MS_WAITLIST_URL=http://localhost:8083       # descomentar cuando esté disponible

FRONTEND_URL=http://localhost:3001          # CORS — en prod: https://frontend.vercel.app
PORT=8090                                   # Puerto del BFF
```

---

## Instalación y ejecución

### Prerrequisitos
- Java 21+
- Gradle 8.x (`brew install gradle` o usar el wrapper)
- ms-auth corriendo en :8080
- ms-user corriendo en :8081

### Generar el Gradle wrapper (primera vez)
```bash
gradle wrapper --gradle-version 8.11.1
```

### Desarrollo
```bash
./gradlew bootRun
# → http://localhost:8090
```

### Build
```bash
./gradlew build
java -jar build/libs/bff-0.1.0.jar
```

### Tests
```bash
./gradlew test
```

---

## Agregar un nuevo microservicio

Cuando ms-appointment o ms-waitlist estén listos, el proceso es:

1. Definir `MS_APPOINTMENTS_URL` (ya está en `application.properties`)
2. El bean `appointmentClient` ya existe en `RestClientConfig.java`
3. El `AppointmentService` ya realiza las llamadas HTTP — solo conectar el servicio real
4. Todos los modelos y endpoints del controller ya están definidos

Para un microservicio completamente nuevo (ej. ms-notifications):
- Agregar variable en `application.properties`
- Agregar `@Bean` en `RestClientConfig.java`
- Crear `model/request/` y `model/response/`
- Crear `service/NotificationService.java`
- Crear `controller/NotificationController.java`

---

## Despliegue en producción

```
Frontend (Vercel) → BFF (Railway :8090) → ms-auth (Railway :8080)
                                         → ms-user (Railway :8081)
```

Variables en Railway para el BFF:
| Variable             | Valor                                            |
|----------------------|--------------------------------------------------|
| MS_AUTH_URL          | https://ms-auth-production-38c7.up.railway.app   |
| MS_USER_URL          | https://ms-user-production.up.railway.app        |
| FRONTEND_URL         | https://[tu-frontend].vercel.app                 |
