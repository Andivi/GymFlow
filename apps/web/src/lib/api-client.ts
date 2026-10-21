import type { components } from "./api-types";

export type Problem = components["schemas"]["Problem"];
export type SystemInfo = components["schemas"]["SystemInfo"];

export class ApiError extends Error {
  readonly status: number;
  readonly problem: Problem | null;

  constructor(status: number, problem: Problem | null) {
    super(problem?.detail ?? problem?.title ?? `Request failed with status ${status}`);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
  }
}

/**
 * Calls the GymFlow API on the same origin. Never put a gym or tenant id in the request:
 * the API derives the tenant from the authenticated session.
 */
export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }

  const response = await fetch(path, { credentials: "same-origin", ...init, headers });
  if (!response.ok) {
    throw new ApiError(response.status, await readProblem(response));
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function readProblem(response: Response): Promise<Problem | null> {
  if (!(response.headers.get("Content-Type") ?? "").includes("json")) {
    return null;
  }
  try {
    return (await response.json()) as Problem;
  } catch {
    return null;
  }
}
