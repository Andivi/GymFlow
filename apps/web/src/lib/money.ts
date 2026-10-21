/**
 * Formats an amount held in integer minor units (for example pesewas) for display.
 * Amounts are stored and calculated as integers; the division here is for display only.
 */
export function formatMoney(amountMinor: number, currency: string, locale = "en-GH"): string {
  const formatter = new Intl.NumberFormat(locale, { style: "currency", currency });
  const fractionDigits = formatter.resolvedOptions().maximumFractionDigits ?? 2;
  return formatter.format(amountMinor / 10 ** fractionDigits);
}
