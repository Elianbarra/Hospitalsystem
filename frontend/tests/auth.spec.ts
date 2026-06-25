import { test, expect } from "@playwright/test";

// ── Respuestas simuladas de la BFF ────────────────────────────────────────────

const PATIENT_SESSION = {
  token: "fake-jwt-patient",
  userId: "patient-001",
  role: "PATIENT",
  email: "paciente@hospital.cl",
};

const DOCTOR_SESSION = {
  token: "fake-jwt-doctor",
  userId: "doctor-001",
  role: "DOCTOR",
  email: "doctor@hospital.cl",
};

const PATIENT_PROFILE = {
  id: "patient-001",
  firstName: "María",
  lastName: "González",
  email: "paciente@hospital.cl",
  phone: "123456789",
  documentType: "DNI",
  documentNumber: "12345678",
  role: "PATIENT",
  isActive: true,
  createdAt: "2025-01-01T00:00:00Z",
};

const DOCTOR_PROFILE = {
  id: "doctor-001",
  firstName: "Juan",
  lastName: "Pérez",
  email: "doctor@hospital.cl",
  phone: "987654321",
  documentType: "DNI",
  documentNumber: "87654321",
  role: "DOCTOR",
  specialty: "CARDIOLOGY",
  isActive: true,
  createdAt: "2025-01-01T00:00:00Z",
};

// ── Helpers ───────────────────────────────────────────────────────────────────

async function mockEndpoints(page: any, profile: object) {
  await page.route("**/api/users/**", (route: any) =>
    route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify(profile) })
  );
  await page.route("**/api/appointments/**", (route: any) =>
    route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify([]) })
  );
  await page.route("**/api/waitlist/**", (route: any) =>
    route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify([]) })
  );
  await page.route("**/api/users?role=DOCTOR**", (route: any) =>
    route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify([]) })
  );
}

async function injectSession(page: any, session: object) {
  await page.evaluate((s: any) => {
    sessionStorage.setItem("token", s.token);
    sessionStorage.setItem("userId", s.userId);
    sessionStorage.setItem("role", s.role);
    sessionStorage.setItem("email", s.email);
  }, session);
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe("Autenticación y control de roles", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await page.evaluate(() => {
      sessionStorage.clear();
      localStorage.clear();
    });
  });

  // 1. Login exitoso como Paciente
  test("Paciente puede iniciar sesión exitosamente", async ({ page }) => {
    await page.route("**/api/auth/login", (route) =>
      route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(PATIENT_SESSION),
      })
    );

    await page.fill('input[type="email"]', "paciente@hospital.cl");
    await page.fill('input[type="password"]', "Password123!");
    await page.click('button[type="submit"]');

    await page.waitForURL("**/dashboard");
    await expect(page).toHaveURL(/\/dashboard/);
  });

  // 2. Login exitoso como Doctor
  test("Doctor puede iniciar sesión exitosamente", async ({ page }) => {
    await page.route("**/api/auth/login", (route) =>
      route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(DOCTOR_SESSION),
      })
    );

    await page.fill('input[type="email"]', "doctor@hospital.cl");
    await page.fill('input[type="password"]', "Password123!");
    await page.click('button[type="submit"]');

    await page.waitForURL("**/dashboard");
    await expect(page).toHaveURL(/\/dashboard/);
  });

  // 3. Credenciales incorrectas muestran error y no redirigen
  test("Credenciales incorrectas muestran error", async ({ page }) => {
    await page.route("**/api/auth/login", (route) =>
      route.fulfill({
        status: 401,
        contentType: "application/json",
        body: JSON.stringify({ message: "Credenciales incorrectas" }),
      })
    );

    await page.fill('input[type="email"]', "incorrecto@hospital.cl");
    await page.fill('input[type="password"]', "clavemalal");
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL("/");
    await expect(page.getByText("Credenciales incorrectas")).toBeVisible();
  });

  // 4. Sin sesión activa se rechaza el acceso al dashboard
  test("Sin sesión activa el dashboard redirige al login", async ({ page }) => {
    await page.goto("/dashboard");
    await page.waitForURL("/");
    await expect(page).toHaveURL("/");
  });

  // 5. Paciente logueado NO ve controles exclusivos de Doctor
  test("Paciente no puede usar funciones de médico", async ({ page }) => {
    await mockEndpoints(page, PATIENT_PROFILE);
    await injectSession(page, PATIENT_SESSION);

    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/dashboard/);

    // Navegar al tab de citas
    await page.getByRole("button", { name: "Mis citas" }).click();

    // Paciente ve "Mis citas médicas", no "Citas asignadas" (exclusivo del doctor)
    await expect(page.getByText("Mis citas médicas")).toBeVisible();
    await expect(page.getByText("Citas asignadas")).not.toBeVisible();

    // Paciente puede crear citas (botón "+ Nueva cita" visible)
    await expect(page.getByRole("button", { name: /nueva cita/i })).toBeVisible();
  });

  // 6. Doctor logueado NO ve el botón de crear cita (ese es solo del paciente)
  test("Doctor no puede crear citas como paciente", async ({ page }) => {
    await mockEndpoints(page, DOCTOR_PROFILE);
    await injectSession(page, DOCTOR_SESSION);

    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/dashboard/);

    // Navegar al tab de citas
    await page.getByRole("button", { name: "Mis citas" }).click();

    // Doctor ve "Citas asignadas"
    await expect(page.getByText("Citas asignadas")).toBeVisible();

    // Doctor NO ve el botón "+ Nueva cita" (isStaff = true, botón oculto)
    await expect(page.getByRole("button", { name: /nueva cita/i })).not.toBeVisible();
  });
});
