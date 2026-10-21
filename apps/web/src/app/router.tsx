import { createBrowserRouter, Navigate, type RouteObject } from "react-router";

import { AppLayout } from "../components/AppLayout";
import { DashboardPage } from "../features/dashboard/DashboardPage";
import { LoginPage } from "../pages/LoginPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { PaymentPage } from "../pages/PaymentPage";

// Client-side route guards are a UX aid only; the API authorizes every request itself.
export const routes: RouteObject[] = [
  { path: "/", element: <Navigate to="/app" replace /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/pay/:token", element: <PaymentPage /> },
  {
    path: "/app",
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="dashboard" replace /> },
      { path: "dashboard", element: <DashboardPage /> },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
];

export function createAppRouter() {
  return createBrowserRouter(routes);
}
