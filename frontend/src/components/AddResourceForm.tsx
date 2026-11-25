import { useId, useState, type FormEvent } from 'react';
import type { Resource } from '../api/client';
import type { Action } from '../App';
import { parseQuantity } from '../validation';

type Props = {
  workItemUnit: string;
  available: Resource[];
  onAdd: (resourceId: number, quantity: number) => Action;
};

export function AddResourceForm({ workItemUnit, available, onAdd }: Props) {
  const id = useId();
  const [resourceId, setResourceId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [error, setError] = useState<string>();
  const [pending, setPending] = useState(false);

  if (available.length === 0) {
    return <p className="muted add-form__done">Every resource is already used by this work item.</p>;
  }

  const selected = available.find((r) => String(r.id) === resourceId);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!selected) return setError('Choose a resource.');
    const parsed = parseQuantity(quantity);
    if (!parsed.ok) return setError(parsed.error);

    setPending(true);
    const failure = await onAdd(selected.id, parsed.value);
    setPending(false);
    setError(failure);
    if (!failure) {
      setResourceId('');
      setQuantity('');
    }
  };

  return (
    <form className="add-form" onSubmit={submit} noValidate>
      <div className="field">
        <label htmlFor={`${id}-resource`}>Resource</label>
        <select id={`${id}-resource`} value={resourceId} onChange={(e) => setResourceId(e.target.value)}>
          <option value="">Choose a resource</option>
          {available.map((r) => (
            <option key={r.id} value={r.id}>
              {r.name} ({r.unit})
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <label htmlFor={`${id}-quantity`}>
          Quantity per {workItemUnit}
          {selected && <span className="muted"> in {selected.unit}</span>}
        </label>
        <input
          id={`${id}-quantity`}
          inputMode="decimal"
          autoComplete="off"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? `${id}-error` : undefined}
        />
      </div>
      <button type="submit" className="button" disabled={pending}>
        {pending ? 'Adding…' : 'Add resource'}
      </button>
      {error && (
        <p id={`${id}-error`} role="alert" className="field-error">
          {error}
        </p>
      )}
    </form>
  );
}
