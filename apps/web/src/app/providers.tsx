import { QueryClientProvider, type QueryClient } from "@tanstack/react-query";
import { useState, type ReactNode } from "react";

import { createQueryClient } from "./query-client";

type AppProvidersProps = {
  children: ReactNode;
  queryClient?: QueryClient;
};

export function AppProviders({ children, queryClient }: AppProvidersProps) {
  const [client] = useState(() => queryClient ?? createQueryClient());
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
