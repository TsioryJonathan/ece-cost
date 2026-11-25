import { useCallback, useEffect, useState } from 'react';
import { api, problemMessage, type Resource, type WorkItem } from './api/client';
import { WorkItemCard } from './components/WorkItemCard';
import { ResourcePanel } from './components/ResourcePanel';

/** Returns an error message, or undefined on success. */
export type Action = Promise<string | undefined>;

const UNREACHABLE = 'Cannot reach the API. Is the backend running?';

export default function App() {
  const [workItems, setWorkItems] = useState<WorkItem[]>();
  const [resources, setResources] = useState<Resource[]>();
  const [loadError, setLoadError] = useState<string>();
  const [notice, setNotice] = useState<string>();

  const load = useCallback(async () => {
    try {
      const [items, res] = await Promise.all([api.GET('/api/work-items'), api.GET('/api/resources')]);
      if (!items.data || !res.data) {
        setLoadError(problemMessage(items.error ?? res.error));
        return;
      }
      setWorkItems(items.data);
      setResources(res.data);
      setLoadError(undefined);
    } catch {
      setLoadError(UNREACHABLE);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const addResource = async (workItemId: number, resourceId: number, quantity: number): Action => {
    try {
      const { data, error } = await api.POST('/api/work-items/{workItemId}/resources', {
        params: { path: { workItemId } },
        body: { resourceId, quantity },
      });
      if (!data) return problemMessage(error);
      setWorkItems((items) => items?.map((item) => (item.id === data.id ? data : item)));
      setNotice(undefined);
      return undefined;
    } catch {
      return UNREACHABLE;
    }
  };

  const updatePrice = async (resource: Resource, unitPrice: number): Action => {
    try {
      const { data, error } = await api.PATCH('/api/resources/{resourceId}', {
        params: { path: { resourceId: resource.id } },
        body: { unitPrice },
      });
      if (!data) return problemMessage(error);
      const affected = workItems?.filter((item) => item.lines.some((l) => l.resourceId === resource.id)).length ?? 0;
      await load();
      setNotice(`${data.name} price updated. ${affected} work item${affected === 1 ? '' : 's'} recalculated.`);
      return undefined;
    } catch {
      return UNREACHABLE;
    }
  };

  return (
    <div className="app">
      <header className="topbar">
        <span className="mark" aria-hidden="true">EB</span>
        <span className="product">ECE BTP</span>
        <span className="scope">Estimating</span>
      </header>

      <main className="canvas">
        <div className="title-row">
          <h1>Price breakdown</h1>
          <p className="muted">Selling price = direct cost × 1.35. Amounts in ariary, per unit of work item.</p>
        </div>

        {loadError && (
          <p role="alert" className="banner banner--error">
            {loadError}{' '}
            <button type="button" className="link" onClick={() => void load()}>
              Retry
            </button>
          </p>
        )}
        {notice && (
          <p role="status" className="banner">
            {notice}
          </p>
        )}

        {!workItems || !resources ? (
          !loadError && <p className="muted">Loading price breakdown…</p>
        ) : (
          <div className="layout">
            <section aria-labelledby="work-items-title">
              <h2 id="work-items-title">Work items</h2>
              <div className="stack">
                {workItems.map((item) => (
                  <WorkItemCard key={item.id} item={item} resources={resources} onAdd={addResource} />
                ))}
              </div>
            </section>
            <ResourcePanel resources={resources} onUpdatePrice={updatePrice} />
          </div>
        )}
      </main>
    </div>
  );
}
