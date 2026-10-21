import { Link } from "react-router";

export function NotFoundPage() {
  return (
    <main className="centered">
      <h1>Page not found</h1>
      <Link to="/app">Back to the dashboard</Link>
    </main>
  );
}
