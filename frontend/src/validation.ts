export type Parsed = { ok: true; value: number } | { ok: false; error: string };

const QUANTITY = /^\d+([.,]\d{1,4})?$/;
const PRICE = /^\d+([.,]\d{1,2})?$/;

const toNumber = (input: string) => Number(input.trim().replace(',', '.'));

/** Quantity per unit of work item: required, strictly positive, up to 4 decimals. */
export function parseQuantity(input: string): Parsed {
  const text = input.trim();
  if (text === '') return { ok: false, error: 'Quantity is required.' };
  if (!QUANTITY.test(text)) return { ok: false, error: 'Quantity must be a number with at most 4 decimals.' };
  const value = toNumber(text);
  if (value <= 0) return { ok: false, error: 'Quantity must be strictly positive.' };
  return { ok: true, value };
}

/** Resource unit price in ariary: required, zero or more, up to 2 decimals. */
export function parsePrice(input: string): Parsed {
  const text = input.trim();
  if (text === '') return { ok: false, error: 'Unit price is required.' };
  if (!PRICE.test(text)) return { ok: false, error: 'Unit price must be a positive number with at most 2 decimals.' };
  return { ok: true, value: toNumber(text) };
}
