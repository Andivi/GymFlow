import { QueryClient } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";

import { AppProviders } from "./providers";
import { routes } from "./router";

function renderAt(path: string) {
  const router = createMemoryRouter(routes, { initialEntries: [path] });
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <AppProviders queryClient={queryClient}>
      <RouterProvider router={router} />
    </AppProviders>,
  );
  return router;
}

describe("routes", () => {
  it("redirects /app to the dashboard and shows the API connection", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      Response.json({ name: "GymFlow API", version: "0.1.0", apiVersion: "v1" }),
    );

    const router = renderAt("/app");

    expect(await screen.findByText("Connected to GymFlow API 0.1.0")).toBeInTheDocument();
    expect(router.state.location.pathname).toBe("/app/dashboard");
  });

  it("reports an unreachable API", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response("", { status: 503 }));

    renderAt("/app/dashboard");

    expect(await screen.findByText("The API is unreachable.")).toBeInTheDocument();
  });

  it("keeps the public payment page outside the staff layout", () => {
    renderAt("/pay/some-token");

    expect(screen.getByRole("heading", { name: "Renew your membership" })).toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "Main" })).not.toBeInTheDocument();
  });

  it("shows a not-found page for unknown routes", () => {
    renderAt("/nowhere");

    expect(screen.getByRole("heading", { name: "Page not found" })).toBeInTheDocument();
  });
});
