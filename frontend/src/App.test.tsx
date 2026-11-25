import { describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import type { Resource, WorkItem } from './api/client';

const cement: Resource = { id: 1, name: 'Cement', type: 'MATERIAL', unit: 'bag', unitPrice: 32500 };
const mason: Resource = { id: 5, name: 'Mason', type: 'LABOUR', unit: 'hour', unitPrice: 5000 };

const concrete = (cementPrice = 32500): WorkItem => {
  const direct = 7 * cementPrice + 2 * 5000;
  return {
    id: 1,
    name: 'Foundation concrete',
    unit: 'm³',
    directCost: direct,
    salePrice: Math.round(direct * 1.35),
    lines: [
      { resourceId: 1, resourceName: 'Cement', resourceType: 'MATERIAL', unit: 'bag', unitPrice: cementPrice, quantity: 7, lineCost: 7 * cementPrice },
      { resourceId: 5, resourceName: 'Mason', resourceType: 'LABOUR', unit: 'hour', unitPrice: 5000, quantity: 2, lineCost: 10000 },
    ],
  };
};
const wall: WorkItem = { id: 2, name: 'Brick wall', unit: 'm²', directCost: 0, salePrice: 0, lines: [] };

const json = (body: unknown, status = 200, type = 'application/json') =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': type } });

/** Stubs fetch with a tiny in-memory API and records every call. */
function stubApi() {
  let cementPrice = 32500;
  let wallItem = wall;
  const calls: string[] = [];
  const fetchMock = vi.fn(async (request: Request) => {
    const { pathname } = new URL(request.url);
    const call = `${request.method} ${pathname}`;
    calls.push(call);
    if (call === 'GET /api/work-items') return json([concrete(cementPrice), wallItem]);
    if (call === 'GET /api/resources') return json([{ ...cement, unitPrice: cementPrice }, mason]);
    if (call === 'PATCH /api/resources/1') {
      cementPrice = (await request.json()).unitPrice;
      return json({ ...cement, unitPrice: cementPrice });
    }
    if (call === 'POST /api/work-items/2/resources') {
      const { resourceId, quantity } = await request.json();
      if (resourceId !== 5) return json({ status: 409, detail: 'Resource is already used.' }, 409, 'application/problem+json');
      wallItem = {
        ...wall,
        directCost: quantity * 5000,
        salePrice: Math.round(quantity * 5000 * 1.35),
        lines: [{ resourceId: 5, resourceName: 'Mason', resourceType: 'LABOUR', unit: 'hour', unitPrice: 5000, quantity, lineCost: quantity * 5000 }],
      };
      return json(wallItem, 201);
    }
    return json({ status: 404, detail: 'Not found.' }, 404, 'application/problem+json');
  });
  vi.stubGlobal('fetch', fetchMock);
  return { calls };
}

const card = (name: string) => screen.getByRole('article', { name });

describe('App', () => {
  it('lists work items with direct cost and selling price from the API', async () => {
    stubApi();
    render(<App />);

    expect(await screen.findByText('Foundation concrete')).toBeTruthy();
    expect(screen.getByTestId('direct-cost-1').textContent).toBe('237,500 Ar');
    expect(screen.getByTestId('sale-price-1').textContent).toBe('320,625 Ar');
    expect(within(card('Brick wall')).getByText(/No resources yet/)).toBeTruthy();
  });

  it('refuses a quantity of 0 without calling the API', async () => {
    const { calls } = stubApi();
    const user = userEvent.setup();
    render(<App />);
    const wallCard = await screen.findByRole('article', { name: 'Brick wall' });

    await user.selectOptions(within(wallCard).getByLabelText('Resource'), 'Mason (hour)');
    await user.type(within(wallCard).getByLabelText(/Quantity per/), '0');
    await user.click(within(wallCard).getByRole('button', { name: 'Add resource' }));

    expect(within(wallCard).getByRole('alert').textContent).toBe('Quantity must be strictly positive.');
    expect(calls.some((c) => c.startsWith('POST'))).toBe(false);
  });

  it('adds a resource and shows the recomputed prices', async () => {
    stubApi();
    const user = userEvent.setup();
    render(<App />);
    const wallCard = await screen.findByRole('article', { name: 'Brick wall' });

    await user.selectOptions(within(wallCard).getByLabelText('Resource'), 'Mason (hour)');
    await user.type(within(wallCard).getByLabelText(/Quantity per/), '1,5');
    await user.click(within(wallCard).getByRole('button', { name: 'Add resource' }));

    expect(await within(wallCard).findByText('Mason')).toBeTruthy();
    expect(screen.getByTestId('direct-cost-2').textContent).toBe('7,500 Ar');
    expect(screen.getByTestId('sale-price-2').textContent).toBe('10,125 Ar');
  });

  it('shows the API problem detail when adding fails', async () => {
    stubApi();
    const user = userEvent.setup();
    render(<App />);
    const wallCard = await screen.findByRole('article', { name: 'Brick wall' });

    await user.selectOptions(within(wallCard).getByLabelText('Resource'), 'Cement (bag)');
    await user.type(within(wallCard).getByLabelText(/Quantity per/), '2');
    await user.click(within(wallCard).getByRole('button', { name: 'Add resource' }));

    expect((await within(wallCard).findByRole('alert')).textContent).toBe('Resource is already used.');
  });

  it('refreshes work items after a resource price change', async () => {
    const { calls } = stubApi();
    const user = userEvent.setup();
    render(<App />);

    await user.click(await screen.findByRole('button', { name: 'Edit unit price of Cement' }));
    const input = screen.getByLabelText('New unit price for Cement');
    await user.clear(input);
    await user.type(input, '35000');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect((await screen.findByRole('status')).textContent).toBe('Cement price updated. 1 work item recalculated.');
    expect(screen.getByTestId('direct-cost-1').textContent).toBe('255,000 Ar');
    expect(screen.getByTestId('sale-price-1').textContent).toBe('344,250 Ar');
    expect(calls.filter((c) => c === 'GET /api/work-items')).toHaveLength(2);
  });

  it('refuses a negative price without calling the API', async () => {
    const { calls } = stubApi();
    const user = userEvent.setup();
    render(<App />);

    await user.click(await screen.findByRole('button', { name: 'Edit unit price of Cement' }));
    const input = screen.getByLabelText('New unit price for Cement');
    await user.clear(input);
    await user.type(input, '-1');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(screen.getByRole('alert').textContent).toMatch(/Unit price must be/);
    expect(calls.some((c) => c.startsWith('PATCH'))).toBe(false);
  });
});
