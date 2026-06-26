import { test, expect } from "@playwright/test";

/**
 * Tests E2E del flujo de autenticación.
 * Requiere que el frontend esté corriendo en http://localhost:3001
 * y el BFF en http://localhost:8090.
 */

test.describe("Flujo de login", () => {

  test("muestra error con credenciales inválidas", async ({ page }) => {
    await page.goto("/");

    // Seleccionar tipo de usuario
    await page.getByText("Soy paciente").click();

    // Ingresar credenciales incorrectas
    await page.getByPlaceholder("Correo electrónico").fill("usuario@falso.cl");
    await page.getByPlaceholder("Contraseña").fill("ClaveIncorrecta123");

    // Enviar formulario
    await page.getByRole("button", { name: "Iniciar sesión" }).click();

    // Verificar mensaje de error
    await expect(
      page.getByText("Credenciales inválidas. Por favor, ingresa correo y contraseña válidos.")
    ).toBeVisible({ timeout: 10000 });
  });

  test("bloquea a un doctor que intenta ingresar como paciente", async ({ page }) => {
    await page.goto("/");

    // Seleccionar tipo: Paciente (aunque las credenciales son de doctor)
    await page.getByText("Soy paciente").click();

    // Ingresar credenciales de un doctor
    await page.getByPlaceholder("Correo electrónico").fill("andres.munoz@rednorte.cl");
    await page.getByPlaceholder("Contraseña").fill("Doctor123!");

    // Enviar formulario
    await page.getByRole("button", { name: "Iniciar sesión" }).click();

    // El sistema debe bloquear el acceso y mostrar mensaje de error
    await expect(
      page.getByText("Esta cuenta pertenece a un funcionario")
    ).toBeVisible({ timeout: 10000 });
  });

  test("login exitoso como doctor redirige al dashboard", async ({ page }) => {
    await page.goto("/");

    // Seleccionar tipo: Funcionario
    await page.getByText("Soy funcionario").click();

    // Ingresar credenciales del doctor
    await page.getByPlaceholder("Correo institucional").fill("andres.munoz@rednorte.cl");
    await page.getByPlaceholder("Contraseña").fill("Doctor123!");

    // Enviar formulario
    await page.getByRole("button", { name: "Iniciar sesión" }).click();

    // Verificar redirección al dashboard
    await expect(page).toHaveURL(/dashboard/, { timeout: 10000 });
  });

});
