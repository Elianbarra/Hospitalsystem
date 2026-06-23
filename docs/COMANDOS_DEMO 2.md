# Comandos Demo — Hospital System

---

## 1. LEVANTAR BACKEND (Kubernetes)

```bash
cd ~/Documents/Hospitalsystem
./deploy.sh
```

Verificar que todos los pods estén Running:
```bash
kubectl get pods -n hospital
```

Port-forward del BFF (dejarlo corriendo en terminal separada):
```bash
kubectl port-forward svc/bff-service -n hospital 8090:8090
```

---

## 2. LEVANTAR FRONTEND

```bash
cd ~/Documents/Hospitalsystem/Frontend
npm run dev
```

Abre: http://localhost:3001

---

## 3. TESTS FRONTEND (Playwright — visual)

> El frontend debe estar corriendo antes de ejecutar

```bash
cd ~/Documents/Hospitalsystem/Frontend
npm run test:e2e
```

Ver reporte HTML:
```bash
npx playwright show-report
```

---

## 4. TESTS BACKEND

```bash
# Ms-User
cd ~/Documents/Hospitalsystem/Ms-User && ./gradlew test

# Ms-Appointment
cd ~/Documents/Hospitalsystem/Ms-appointment && ./gradlew test

# Ms-Waitlist
cd ~/Documents/Hospitalsystem/Ms-Waitlist && ./gradlew test
```

---

## 5. REBUILD UN MICROSERVICIO (cuando hay cambios de código)

```bash
cd ~/Documents/Hospitalsystem/<Microservicio>
./gradlew build -x test
docker build -t <nombre>:latest .
kubectl rollout restart deployment/<nombre> -n hospital
kubectl rollout status deployment/<nombre> -n hospital
```

Nombres: `ms-auth` · `ms-user` · `ms-appointment` · `ms-waitlist` · `bff`

---

## 6. LIMPIAR CITAS (para demo limpia)

```bash
psql "postgresql://neondb_owner:NuevaClave2025!@ep-patient-silence-ad1s4w9m-pooler.c-2.us-east-1.aws.neon.tech/msappointment_db?sslmode=require" \
  -c "TRUNCATE TABLE appointments RESTART IDENTITY;"
```

---

## 7. CREDENCIALES DEMO

| Rol | Email | Contraseña |
|-----|-------|-----------|
| Admin | admin@rednorte.cl | Admin123! |
| Doctor (Cardiología) | andres.munoz@rednorte.cl | Doctor123! |
| Doctor (Cardiología) | carmen.reyes@rednorte.cl | Doctor123! |
| Doctor (Neurología) | jorge.espinoza@rednorte.cl | Doctor123! |

> Los pacientes se registran en vivo durante la demo en http://localhost:3001/register

---

## 8. PENDIENTES ANTES DE LA DEMO

- [ ] Correr `./deploy.sh` completo para aplicar cambios de ms-appointment y BFF
- [ ] Insertar admin en NeonDB (ver instrucciones en CREDENCIALES o correr V3 migrations)
- [ ] Verificar tests backend con `./gradlew test` en cada microservicio
- [ ] Limpiar tabla appointments si hay seed data
