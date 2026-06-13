# Salud RedNorte — Portal del Paciente

Frontend del portal de salud RedNorte. Permite a los pacientes iniciar sesión, ver su perfil clínico y gestionar su cuenta. Los administradores pueden registrar personal médico.

## Stack

- **Next.js 16** (App Router) · **React 19** · **TypeScript**
- **Tailwind CSS 3**
- Corre en el puerto **3001**

## Requisitos previos

- Node.js 18+
- BFF (Backend for Frontend) corriendo en `http://localhost:3000` (o la URL que configures en `.env.local`)

## Instalación

```bash
npm install
```

Crea el archivo de variables de entorno:

```bash
cp .env.local.example .env.local
```

Edita `.env.local` y ajusta la URL del BFF si es necesario:

```env
NEXT_PUBLIC_BFF_URL=http://localhost:3000
```

## Scripts

| Comando | Descripción |
|---|---|
| `npm run dev` | Inicia en modo desarrollo (puerto 3001) |
| `npm run build` | Genera la build de producción |
| `npm run start` | Inicia la build de producción (puerto 3001) |

## Páginas

| Ruta | Descripción | Acceso |
|---|---|---|
| `/` | Landing + formulario de login | Público |
| `/register` | Registro de pacientes | Público |
| `/register/staff` | Registro de personal médico | Solo `ADMIN` |
| `/dashboard` | Perfil del usuario autenticado | Autenticado |

## Autenticación

El login obtiene un token JWT desde el BFF. Según la opción "Mantener la sesión":

- **Marcado** → persiste en `localStorage`
- **Desmarcado** → persiste en `sessionStorage`

La sesión almacena: `token`, `userId`, `email`, `role`.

## Roles disponibles

`PATIENT` · `DOCTOR` · `NURSE` · `ADMIN` · `RECEPTIONIST`

## Endpoints del BFF consumidos

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/auth/login` | Autenticación |
| `POST` | `/api/users` | Registro de usuario |
| `GET` | `/api/users/:id` | Perfil del usuario |

## Estructura del proyecto

```
src/
├── app/
│   ├── components/        # Componentes reutilizables (LoginForm, Alert, HospitalLogo…)
│   ├── dashboard/         # Página de perfil autenticado
│   ├── register/          # Registro de pacientes y personal
│   ├── globals.css
│   └── layout.tsx
├── features/
│   └── users/             # Constantes de roles y tipos de documento
└── lib/
    ├── bff.ts             # URL base del BFF
    ├── session.ts         # Lectura y limpieza de sesión
    └── styles.ts          # Clases Tailwind compartidas
```
