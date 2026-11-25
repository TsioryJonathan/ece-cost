// Stateful in-memory mock of docs/openapi.yaml. Zero dependencies. Run: node server.mjs (PORT, default 8081).
import { createServer } from 'node:http';

const resources = [
  [1, 'Cement', 'MATERIAL', 'bag', 32500], [2, 'Sand', 'MATERIAL', 'm³', 45000],
  [3, 'Gravel', 'MATERIAL', 'm³', 60000], [4, 'Rebar HA10', 'MATERIAL', 'kg', 6500],
  [5, 'Mason', 'LABOR', 'hour', 5000], [6, 'Laborer', 'LABOR', 'hour', 3000],
  [7, 'Concrete mixer', 'EQUIPMENT', 'hour', 15000],
].map(([id, name, type, unit, unitPrice]) => ({ id, name, type, unit, unitPrice }));

const workItems = [
  [1, 'Foundation concrete', 'm³', { 1: 7, 2: 0.4, 3: 0.8, 5: 2, 6: 4, 7: 1 }],
  [2, 'Cement plaster', 'm²', { 1: 0.3, 2: 0.02, 5: 0.5 }],
  [3, 'Reinforced slab', 'm²', { 1: 1.2, 2: 0.06, 3: 0.12, 4: 8, 5: 1.5, 6: 2 }],
  [4, 'Brick wall', 'm²', {}],
].map(([id, name, unit, lines]) => ({
  id, name, unit,
  lines: new Map(Object.entries(lines).map(([rid, q]) => [Number(rid), q])),
}));

const round = (x) => Math.round(x + Number.EPSILON * x);
const findResource = (id) => resources.find((r) => r.id === id);

function toDto(wi) {
  const exact = [...wi.lines].map(([rid, quantity]) => {
    const r = findResource(rid);
    return { r, quantity, cost: quantity * r.unitPrice };
  }).sort((a, b) => a.r.name.localeCompare(b.r.name));
  const direct = exact.reduce((sum, l) => sum + l.cost, 0);
  return {
    id: wi.id, name: wi.name, unit: wi.unit,
    directCost: round(direct), salePrice: round(direct * 1.35),
    lines: exact.map(({ r, quantity, cost }) => ({
      resourceId: r.id, resourceName: r.name, resourceType: r.type, unit: r.unit,
      unitPrice: r.unitPrice, quantity, lineCost: round(cost),
    })),
  };
}

class HttpError extends Error {
  constructor(status, title, detail) { super(detail); this.status = status; this.title = title; }
}
const bad = (detail) => new HttpError(400, 'Bad Request', detail);
const notFound = (detail) => new HttpError(404, 'Not Found', detail);
const isNumber = (v) => typeof v === 'number' && Number.isFinite(v);

function parseBody(raw) {
  try {
    const body = JSON.parse(raw);
    if (body && typeof body === 'object' && !Array.isArray(body)) return body;
  } catch { /* falls through */ }
  throw bad('Request body must be a valid JSON object.');
}

function addLine(workItemId, raw) {
  const { resourceId, quantity } = parseBody(raw);
  if (!Number.isInteger(resourceId)) throw bad('Resource id must be an integer.');
  if (!isNumber(quantity) || quantity <= 0) throw bad('Quantity must be strictly positive.');
  const wi = workItems.find((w) => w.id === workItemId);
  if (!wi) throw notFound(`Work item ${workItemId} not found.`);
  if (!findResource(resourceId)) throw notFound(`Resource ${resourceId} not found.`);
  if (wi.lines.has(resourceId)) {
    throw new HttpError(409, 'Conflict', `Resource ${resourceId} is already used by work item ${workItemId}.`);
  }
  wi.lines.set(resourceId, quantity);
  return [201, toDto(wi)];
}

function updatePrice(resourceId, raw) {
  const { unitPrice } = parseBody(raw);
  if (!isNumber(unitPrice) || unitPrice < 0) throw bad('Unit price must be a number greater than or equal to zero.');
  const r = findResource(resourceId);
  if (!r) throw notFound(`Resource ${resourceId} not found.`);
  r.unitPrice = unitPrice;
  return [200, r];
}

function route(method, path, raw) {
  let m;
  if (method === 'GET' && path === '/api/work-items') return [200, workItems.map(toDto)];
  if (method === 'GET' && path === '/api/resources') {
    return [200, [...resources].sort((a, b) => a.name.localeCompare(b.name))];
  }
  if (method === 'POST' && (m = path.match(/^\/api\/work-items\/(\d+)\/resources$/))) return addLine(Number(m[1]), raw);
  if (method === 'PATCH' && (m = path.match(/^\/api\/resources\/(\d+)$/))) return updatePrice(Number(m[1]), raw);
  throw notFound(`No route for ${method} ${path}.`);
}

function send(res, status, body, type = 'application/json') {
  setTimeout(() => {
    res.writeHead(status, { 'Content-Type': type });
    res.end(JSON.stringify(body));
  }, 200);
}

createServer((req, res) => {
  let raw = '';
  req.on('data', (chunk) => { raw += chunk; });
  req.on('end', () => {
    const path = new URL(req.url, 'http://localhost').pathname;
    try {
      const [status, body] = route(req.method, path, raw);
      send(res, status, body);
    } catch (e) {
      if (!(e instanceof HttpError)) throw e;
      const problem = { type: 'about:blank', title: e.title, status: e.status, detail: e.message, instance: path };
      send(res, e.status, problem, 'application/problem+json');
    }
  });
}).listen(Number(process.env.PORT) || 8081, function () {
  console.log(`Mock API on http://localhost:${this.address().port}`);
});
