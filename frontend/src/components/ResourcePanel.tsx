import { useState, type SubmitEvent } from 'react';
import type { Resource } from '../api/client';
import type { Action } from '../App';
import { formatAriary, resourceTypeLabel } from '../format';
import { parsePrice } from '../validation';

type Props = {
  resources: Resource[];
  onUpdatePrice: (resource: Resource, unitPrice: number) => Action;
};

export function ResourcePanel({ resources, onUpdatePrice }: Props) {
  return (
    <section className="panel resources" aria-labelledby="resources-title">
      <h2 id="resources-title">Resource prices</h2>
      <p className="muted">A new price flows into every work item that uses the resource.</p>
      <table className="table">
        <thead>
          <tr>
            <th scope="col">Resource</th>
            <th scope="col" className="num">Unit price</th>
          </tr>
        </thead>
        <tbody>
          {resources.map((r) => (
            <ResourceRow key={r.id} resource={r} onUpdatePrice={onUpdatePrice} />
          ))}
        </tbody>
      </table>
    </section>
  );
}

function ResourceRow({ resource, onUpdatePrice }: { resource: Resource } & Pick<Props, 'onUpdatePrice'>) {
  const [draft, setDraft] = useState<string>();
  const [error, setError] = useState<string>();
  const [pending, setPending] = useState(false);
  const editing = draft !== undefined;

  const cancel = () => {
    setDraft(undefined);
    setError(undefined);
  };

  const save = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    const parsed = parsePrice(draft ?? '');
    if (!parsed.ok) return setError(parsed.error);
    setPending(true);
    const failure = await onUpdatePrice(resource, parsed.value);
    setPending(false);
    if (failure) return setError(failure);
    cancel();
  };

  return (
    <tr>
      <td>
        {resource.name}
        <span className="muted"> per {resource.unit}</span>
        <span className={`tag tag--${resource.type.toLowerCase()}`}>{resourceTypeLabel[resource.type]}</span>
      </td>
      <td className="num">
        {editing ? (
          <form className="price-form" onSubmit={save} noValidate>
            <input
              aria-label={`New unit price for ${resource.name}`}
              inputMode="decimal"
              autoFocus
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={(e) => e.key === 'Escape' && cancel()}
              aria-invalid={error ? true : undefined}
            />
            <button type="submit" className="button button--small" disabled={pending}>
              Save
            </button>
            <button type="button" className="link" onClick={cancel}>
              Cancel
            </button>
            {error && (
              <p role="alert" className="field-error">
                {error}
              </p>
            )}
          </form>
        ) : (
          <button
            type="button"
            className="price"
            aria-label={`Edit unit price of ${resource.name}`}
            onClick={() => setDraft(String(resource.unitPrice))}
          >
            {formatAriary(resource.unitPrice)}
          </button>
        )}
      </td>
    </tr>
  );
}
