import { QueryClient } from "@tanstack/react-query";

import { ApiError } from "../lib/api-client";

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        // Retrying a 4xx only repeats the same answer; retry network and server errors.
        retry: (failureCount, error) =>
          !(error instanceof ApiError && error.status < 500) && failureCount < 2,
      },
    },
  });
}
