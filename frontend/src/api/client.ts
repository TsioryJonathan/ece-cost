import createClient from 'openapi-fetch';
import type { components, paths } from './schema';

export type WorkItem = components['schemas']['WorkItem'];
export type WorkItemLine = components['schemas']['WorkItemLine'];
export type Resource = components['schemas']['Resource'];
export type ResourceType = components['schemas']['ResourceType'];
type Problem = components['schemas']['Problem'];

export const api = createClient<paths>({
  baseUrl: window.location.origin,
  // Resolve fetch at call time so tests can stub it.
  fetch: (request) => globalThis.fetch(request),
});

export function problemMessage(error: Problem | string | undefined): string {
  if (error && typeof error === 'object') return error.detail ?? error.title ?? 'Unexpected error.';
  return 'Unexpected error.';
}
