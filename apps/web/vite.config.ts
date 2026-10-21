import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

const apiTarget = process.env.GYMFLOW_API_URL ?? "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Same-origin proxy so the browser never needs CORS and session cookies stay first-party.
    proxy: {
      "/api": apiTarget,
      "/actuator": apiTarget,
    },
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./src/test/setup.ts"],
  },
});
