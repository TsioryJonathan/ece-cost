import { describe, expect, it } from 'vitest';
import { parsePrice, parseQuantity } from './validation';

describe('parseQuantity', () => {
  it.each([
    ['7', 7],
    ['0.4', 0.4],
    ['1,5', 1.5],
    [' 0.0001 ', 0.0001],
  ])('accepts %j', (input, expected) => {
    expect(parseQuantity(input)).toEqual({ ok: true, value: expected });
  });

  it.each([
    ['', 'Quantity is required.'],
    ['0', 'Quantity must be strictly positive.'],
    ['0.0000', 'Quantity must be strictly positive.'],
    ['-2', 'Quantity must be a number with at most 4 decimals.'],
    ['abc', 'Quantity must be a number with at most 4 decimals.'],
    ['1.23456', 'Quantity must be a number with at most 4 decimals.'],
  ])('rejects %j', (input, error) => {
    expect(parseQuantity(input)).toEqual({ ok: false, error });
  });
});

describe('parsePrice', () => {
  it.each([
    ['32500', 32500],
    ['0', 0],
    ['1250,5', 1250.5],
  ])('accepts %j', (input, expected) => {
    expect(parsePrice(input)).toEqual({ ok: true, value: expected });
  });

  it.each(['', '-1', '12.345', '1e3'])('rejects %j', (input) => {
    expect(parsePrice(input).ok).toBe(false);
  });
});
