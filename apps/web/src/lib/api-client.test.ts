import { ApiError, apiFetch } from "./api-client";

function mockFetch(response: Response) {
  return vi.spyOn(globalThis, "fetch").mockResolvedValue(response);
}

describe("apiFetch", () => {
  it("returns the parsed JSON body", async () => {
    mockFetch(Response.json({ name: "GymFlow API" }));

    await expect(apiFetch("/api/v1/system/info")).resolves.toEqual({ name: "GymFlow API" });
  });

  it("sends same-origin credentials and asks for JSON", async () => {
    const fetchSpy = mockFetch(Response.json({}));

    await apiFetch("/api/v1/system/info");

    const [, init] = fetchSpy.mock.calls[0]!;
    expect(init?.credentials).toBe("same-origin");
    expect(new Headers(init?.headers).get("Accept")).toBe("application/json");
  });

  it("raises an ApiError carrying the problem document", async () => {
    mockFetch(
      new Response(
        JSON.stringify({
          status: 404,
          title: "Not found",
          detail: "Member not found",
          code: "NOT_FOUND",
        }),
        { status: 404, headers: { "Content-Type": "application/problem+json" } },
      ),
    );

    const error = await apiFetch("/api/v1/members/x").catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(404);
    expect((error as ApiError).problem?.code).toBe("NOT_FOUND");
    expect((error as ApiError).message).toBe("Member not found");
  });

  it("raises an ApiError without a problem for non-JSON failures", async () => {
    mockFetch(new Response("Bad gateway", { status: 502 }));

    const error = await apiFetch("/api/v1/system/info").catch((e: unknown) => e);

    expect((error as ApiError).problem).toBeNull();
    expect((error as ApiError).status).toBe(502);
  });
});
