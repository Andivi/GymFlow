import { NavLink, Outlet } from "react-router";

export function AppLayout() {
  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="brand">GymFlow</div>
        <nav aria-label="Main">
          <NavLink to="/app/dashboard">Dashboard</NavLink>
        </nav>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
