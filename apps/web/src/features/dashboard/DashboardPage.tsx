import { useQuery } from "@tanstack/react-query";

import { apiFetch, type SystemInfo } from "../../lib/api-client";

export function DashboardPage() {
  const systemInfo = useQuery({
    queryKey: ["system", "info"],
    queryFn: () => apiFetch<SystemInfo>("/api/v1/system/info"),
  });

  return (
    <section>
      <h1>Dashboard</h1>
      <p className="api-status" role="status">
        {systemInfo.isPending && "Connecting to the API…"}
        {systemInfo.isError && "The API is unreachable."}
        {systemInfo.isSuccess && `Connected to ${systemInfo.data.name} ${systemInfo.data.version}`}
      </p>
    </section>
  );
}
