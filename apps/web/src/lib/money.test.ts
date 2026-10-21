import { formatMoney } from "./money";

describe("formatMoney", () => {
  it("formats Ghana cedi minor units", () => {
    expect(formatMoney(15050, "GHS")).toBe(
      new Intl.NumberFormat("en-GH", { style: "currency", currency: "GHS" }).format(150.5),
    );
  });

  it("respects currencies without minor units", () => {
    expect(formatMoney(500, "JPY", "en-US")).toBe("¥500");
  });
});
