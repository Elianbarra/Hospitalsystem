# ms-waitlist

Microservicio simple de gestión de waitlist con Spring Boot 3.3, Java 21 y Gradle Kotlin DSL. Incluye Docker Compose para ejecución local e manifests de Kubernetes para despliegue en cluster.

---

## 📋 Tabla de contenidos

1. [Stack tecnológico](#stack-tecnológico)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Patrones de diseño](#patrones-de-diseño)
4. [Endpoints](#endpoints)
5. [Modelos de datos](#modelos-de-datos)
6. [Ejecución local](#ejecución-local)
7. [Docker](#docker)
8. [Kubernetes](#kubernetes)
9. [Testing](#testing)
10. [Ejemplos de uso](#ejemplos-de-uso)

---

## 🛠 Stack tecnológico

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **Spring Boot** | 3.3.2 | Framework REST y inyección de dependencias |
| **Java** | 21 | Lenguaje principal |
| **Gradle** | 8.8+ | Build tool con sintaxis Kotlin DSL |
| **JUnit** | 5 | Framework de testing |
| **AssertJ** | - | Assertions para tests |
| **Docker** | - | Containerización |
| **Kubernetes** | - | Orquestación de contenedores |

---

## 📂 Estructura del proyecto

```
ms-waitlist/
├── src/main/java/com/example/waitlist/
│   ├── WaitlistApplication.java              # Punto de entrada Spring Boot
│   ├── controller/
│   │   └── WaitlistController.java            # REST endpoints
│   ├── service/
│   │   └── WaitlistService.java               # Lógica de negocio
│   ├── domain/
│   │   ├── WaitlistEntry.java                 # Entidad principal (record)
│   │   ├── WaitlistTier.java                  # Enum: STANDARD, PRIORITY
│   │   └── WaitlistStatus.java                # Enum: PENDING
│   ├── dto/
│   │   ├── CreateWaitlistRequest.java         # DTO entrada POST
│   │   └── WaitlistResponse.java              # DTO respuesta
│   ├── factory/
│   │   └── WaitlistEntryFactory.java          # Patrón Factory
│   ├── strategy/
│   │   ├── WaitlistTierStrategy.java          # Interface Strategy
│   │   ├── StandardWaitlistTierStrategy.java  # Estrategia STANDARD
│   │   ├── PriorityWaitlistTierStrategy.java  # Estrategia PRIORITY
│   │   └── WaitlistTierStrategyResolver.java  # Resolver de estrategias
│   ├── repository/
│   │   ├── WaitlistRepository.java            # Interface repositorio
│   │   └── InMemoryWaitlistRepository.java    # Implementación en memoria
│   └── resources/
│       └── application.yml                     # Configuración Spring
├── src/test/java/com/example/waitlist/
│   ├── factory/
│   │   └── WaitlistEntryFactoryTest.java
│   └── service/
│       └── WaitlistServiceTest.java
├── k8s/                                       # Manifests Kubernetes
│   ├── namespace.yaml                         # Namespace: waitlist
│   ├── deployment.yaml                        # Deployment del microservicio
│   └── service.yaml                           # ClusterIP Service
├── build.gradle.kts                           # Configuración Gradle
├── Dockerfile                                 # Multi-stage build
├── docker-compose.yml                         # Compose local
└── README.md                                  # Este archivo
```

---

## 🎯 Patrones de diseño

Se utilizan **exactamente 2 patrones** para mantener la arquitectura limpia y sin complejidad innecesaria:

### 1. **Factory Pattern** (`WaitlistEntryFactory`)

**Propósito:** Centralizar la creación y normalización de instancias `WaitlistEntry`.

**Ubicación:** [`src/main/java/com/example/waitlist/factory/WaitlistEntryFactory.java`](src/main/java/com/example/waitlist/factory/WaitlistEntryFactory.java)

**Responsabilidades:**
- Generar UUID único para cada entrada
- Normalizar espacios en `name`
- Convertir `email` a minúsculas y trimear
- Asignar estado inicial (`PENDING`)
- Registrar timestamp en UTC

**Ejemplo de uso:**
```java
WaitlistEntry entry = factory.create(request, WaitlistTier.PRIORITY);
```

### 2. **Strategy Pattern** (`WaitlistTierStrategy`)

**Propósito:** Resolver dinámicamente qué tier corresponde según el valor solicitado, evitando if/else largos en el servicio.

**Componentes:**
- **Interface:** [`WaitlistTierStrategy.java`](src/main/java/com/example/waitlist/strategy/WaitlistTierStrategy.java)
  - `supportedValues()`: Valores que acepta esta estrategia (ej: `"PRIORITY"`, `"VIP"`)
  - `resolveTier()`: Devuelve el enum `WaitlistTier`

- **Implementaciones:**
  - [`StandardWaitlistTierStrategy.java`](src/main/java/com/example/waitlist/strategy/StandardWaitlistTierStrategy.java): Soporta `"STANDARD"`, `"BASIC"`, `"DEFAULT"`
  - [`PriorityWaitlistTierStrategy.java`](src/main/java/com/example/waitlist/strategy/PriorityWaitlistTierStrategy.java): Soporta `"PRIORITY"`, `"VIP"`

- **Resolver:** [`WaitlistTierStrategyResolver.java`](src/main/java/com/example/waitlist/strategy/WaitlistTierStrategyResolver.java)
  - Inyecta todas las estrategias disponibles
  - Construye un mapa normalizado `String → Strategy`
  - Resuelve tier con fallback a STANDARD

**Flujo:**
```
request.tier = "vip"
  ↓
WaitlistTierStrategyResolver.resolve("vip")
  ↓
Normaliza a "VIP" → busca en mapa
  ↓
Encuentra PriorityWaitlistTierStrategy
  ↓
Devuelve WaitlistTier.PRIORITY
```

---

## 🔌 Endpoints

### `POST /api/waitlist`

**Descripción:** Crear una nueva entrada en la waitlist.

**Request:**
```json
{
  "name": "Ana López",
  "email": "ana@example.com",
  "tier": "priority"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Ana López",
  "email": "ana@example.com",
  "tier": "PRIORITY",
  "status": "PENDING",
  "createdAt": "2026-06-10T12:30:45Z"
}
```

**Validaciones:**
- `name`: Requerido, máx 120 caracteres
- `email`: Requerido, válido, máx 160 caracteres
- `tier`: Opcional (default: STANDARD), máx 40 caracteres
- Email único: No se permite duplicado (409 Conflict)

---

### `GET /api/waitlist`

**Descripción:** Obtener todas las entradas ordenadas por fecha de creación.

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Ana López",
    "email": "ana@example.com",
    "tier": "PRIORITY",
    "status": "PENDING",
    "createdAt": "2026-06-10T12:30:45Z"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Carlos Ruiz",
    "email": "carlos@example.com",
    "tier": "STANDARD",
    "status": "PENDING",
    "createdAt": "2026-06-10T13:00:00Z"
  }
]
```

---

### `GET /api/waitlist/{id}`

**Descripción:** Obtener una entrada específica por ID.

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Ana López",
  "email": "ana@example.com",
  "tier": "PRIORITY",
  "status": "PENDING",
  "createdAt": "2026-06-10T12:30:45Z"
}
```

**Errores:**
- **404 Not Found:** Si el ID no existe

---

## 📊 Modelos de datos

### `WaitlistEntry` (Record)

```java
record WaitlistEntry(
    UUID id,
    String name,
    String email,
    WaitlistTier tier,
    WaitlistStatus status,
    OffsetDateTime createdAt
)
```

Implementada como **record** de Java 16+ para inmutabilidad y menos boilerplate.

### Enums

**`WaitlistTier`**
```java
enum WaitlistTier {
    STANDARD,   // Nivel por defecto
    PRIORITY    // Nivel prioritario
}
```

**`WaitlistStatus`**
```java
enum WaitlistStatus {
    PENDING     // Estado inicial
}
```

### DTOs

**`CreateWaitlistRequest`**
- Request para POST con validaciones JSR-380 (Jakarta Validation)

**`WaitlistResponse`**
- Response estándar en todos los endpoints GET/POST

---

## 🚀 Ejecución local

### Prerrequisitos

- Java 21+ instalado
- Gradle 8.8+ (o usar el wrapper `gradlew`)

### Ejecutar directamente

```bash
cd ms-waitlist

# Opción 1: Gradle wrapper (Linux/Mac)
./gradlew bootRun

# Opción 2: Gradle wrapper (Windows)
gradlew.bat bootRun

# Opción 3: Gradle global (si está instalado)
gradle bootRun
```

La aplicación estará disponible en `http://localhost:8080`

### Ejecutar tests

```bash
./gradlew test
```

Salida esperada:
```
> Task :test
WaitlistEntryFactoryTest > shouldNormalizeEmailAndName PASSED
WaitlistServiceTest > shouldCreateWaitlistEntry PASSED

BUILD SUCCESSFUL
```

---

## 🐳 Docker

### Build y run con Docker Compose

```bash
cd ms-waitlist

# Build e inicia el contenedor
docker compose up --build

# En otro terminal, prueba
curl http://localhost:8080/api/waitlist
```

**Detener:**
```bash
docker compose down
```

### Detalles del Dockerfile

```dockerfile
# Stage 1: Build
FROM gradle:8.8-jdk21-alpine AS build
# Compila el JAR con Gradle

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
# Solo incluye JRE, no el JDK completo (imagen más ligera)
```

**Ventajas:**
- Multi-stage build → imagen final ~300MB
- Alpine Linux → base pequeña
- Solo JRE en runtime

---

## ☸️ Kubernetes

### 1. Build y push de la imagen

```bash
# Build local
docker build -t waitlist-api:latest .

# Si usas un registry (Docker Hub, ECR, etc.)
docker tag waitlist-api:latest YOUR_REGISTRY/waitlist-api:latest
docker push YOUR_REGISTRY/waitlist-api:latest
```

### 2. Aplicar manifests

```bash
# Crear namespace y recursos
kubectl apply -f k8s/

# Verificar
kubectl get all -n waitlist
kubectl logs -n waitlist deployment/waitlist-api
```

### 3. Acceder al servicio

```bash
# Port forward (desarrollo)
kubectl port-forward -n waitlist svc/waitlist-api 8080:80

# Luego: curl http://localhost:8080/api/waitlist
```

### Componentes K8s

| Archivo | Descripción |
|---------|-------------|
| `namespace.yaml` | Crea namespace `waitlist` para aislamiento |
| `deployment.yaml` | Define el Deployment con 1 réplica, imagen local |
| `service.yaml` | ClusterIP Service en puerto 80 → 8080 del contenedor |

**Notas:**
- `imagePullPolicy: IfNotPresent` → usa imagen local si existe
- `replicas: 1` → escalable modificando el YAML
- Sin healthchecks aún (próxima iteración)

---

## 🧪 Testing

### Tests incluidos

**`WaitlistEntryFactoryTest`**
```java
@Test
void shouldNormalizeEmailAndName() {
    var request = new CreateWaitlistRequest("  Ana Lopez  ", "  ANA@EXAMPLE.COM  ", null);
    var entry = factory.create(request, WaitlistTier.STANDARD);
    
    assertThat(entry.name()).isEqualTo("Ana Lopez");
    assertThat(entry.email()).isEqualTo("ana@example.com");
}
```

**`WaitlistServiceTest`**
```java
@Test
void shouldCreateWaitlistEntry() {
    var response = waitlistService.create(
        new CreateWaitlistRequest("Lucia", "lucia@example.com", "priority")
    );
    
    assertThat(response.email()).isEqualTo("lucia@example.com");
    assertThat(response.tier().name()).isEqualTo("PRIORITY");
}
```

---

## 📝 Ejemplos de uso

### Ejemplo 1: Agregar entrada STANDARD

```bash
curl -X POST http://localhost:8080/api/waitlist \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan García",
    "email": "juan@example.com"
  }'
```

Response:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Juan García",
  "email": "juan@example.com",
  "tier": "STANDARD",
  "status": "PENDING",
  "createdAt": "2026-06-10T15:45:30Z"
}
```

### Ejemplo 2: Agregar entrada PRIORITY

```bash
curl -X POST http://localhost:8080/api/waitlist \
  -H "Content-Type: application/json" \
  -d '{
    "name": "María Fernández",
    "email": "maria@example.com",
    "tier": "VIP"
  }'
```

La estrategia PriorityWaitlistTierStrategy reconoce `"VIP"` → devuelve `PRIORITY`.

### Ejemplo 3: Listar todas las entradas

```bash
curl http://localhost:8080/api/waitlist
```

### Ejemplo 4: Obtener una entrada por ID

```bash
curl http://localhost:8080/api/waitlist/123e4567-e89b-12d3-a456-426614174000
```

---

## 🏗️ Decisiones arquitectónicas

| Decisión | Razón |
|----------|-------|
| **Records para entidades** | Inmutabilidad, menos boilerplate |
| **Repository pattern** | Cambio fácil a DB sin afectar servicio |
| **Strategy en lugar de if/else** | Escalable: agregar tiers sin modificar WaitlistService |
| **Factory centralizada** | Normalización consistente de datos |
| **InMemoryRepository** | MVP simple; reemplazar por JPA/Spring Data |
| **DTOs separados** | Validación centralizada, API contract claro |
| **Multi-stage Docker** | Imagen final compacta |
| **Alpine Linux** | Base pequeña y segura |

---

## 🔧 Próximas mejoras sugeridas

- [ ] Agregar persistencia con PostgreSQL + Liquibase
- [ ] Health checks y readiness/liveness probes en K8s
- [ ] OpenAPI/Swagger documentation
- [ ] Logging centralizado (SLF4J + Logback)
- [ ] Metrics con Micrometer + Prometheus
- [ ] CircuitBreaker para resiliencia
- [ ] Tests de integración con testcontainers

---

## 📄 Licencia

MIT


