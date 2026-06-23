import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  fullyParallel: false,
  retries: 0,
  reporter: [["html", { outputFolder: "playwright-report", open: "on-failure" }]],
  use: {
    baseURL: "http://localhost:3001",
    headless: false,        // browser visible durante la demo
    slowMo: 600,            // pausa entre acciones para que se vea bien
    screenshot: "on",
    video: "on",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
