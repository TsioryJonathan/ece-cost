import type { Resource, ResourceType, WorkItem } from '../api/client';
import type { Action } from '../App';
import { formatAriary, formatQuantity, resourceTypeLabel } from '../format';
import { AddResourceForm } from './AddResourceForm';

type Props = {
  item: WorkItem;
  resources: Resource[];
  onAdd: (workItemId: number, resourceId: number, quantity: number) => Action;
};

const TYPES: ResourceType[] = ['MATERIAL', 'LABOUR', 'EQUIPMENT'];

export function WorkItemCard({ item, resources, onAdd }: Props) {
  const used = new Set(item.lines.map((l) => l.resourceId));
  const available = resources.filter((r) => !used.has(r.id));
  const titleId = `work-item-${item.id}`;

  return (
    <article className="panel work-item" aria-labelledby={titleId}>
      <header className="work-item__head">
        <div>
          <h3 id={titleId}>{item.name}</h3>
          <p className="muted">Per {item.unit}</p>
        </div>
        <dl className="amounts">
          <div>
            <dt>Direct cost</dt>
            <dd data-testid={`direct-cost-${item.id}`}>{formatAriary(item.directCost)}</dd>
          </div>
          <div className="amounts__sale">
            <dt>Selling price</dt>
            <dd data-testid={`sale-price-${item.id}`}>{formatAriary(item.salePrice)}</dd>
          </div>
        </dl>
      </header>

      {item.lines.length === 0 ? (
        <p className="empty">No resources yet. The cost stays at 0 until one is added.</p>
      ) : (
        <>
          <CostSplit item={item} />
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Resource</th>
                <th scope="col" className="num">Quantity</th>
                <th scope="col" className="num">Unit price</th>
                <th scope="col" className="num">Cost</th>
              </tr>
            </thead>
            <tbody>
              {item.lines.map((line) => (
                <tr key={line.resourceId}>
                  <td>
                    {line.resourceName}
                    <span className={`tag tag--${line.resourceType.toLowerCase()}`}>
                      {resourceTypeLabel[line.resourceType]}
                    </span>
                  </td>
                  <td className="num">
                    {formatQuantity(line.quantity)} <span className="muted">{line.unit}</span>
                  </td>
                  <td className="num">{formatAriary(line.unitPrice)}</td>
                  <td className="num">{formatAriary(line.lineCost)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      <AddResourceForm
        workItemUnit={item.unit}
        available={available}
        onAdd={(resourceId, quantity) => onAdd(item.id, resourceId, quantity)}
      />
    </article>
  );
}

/** Share of the direct cost taken by materials, labour and equipment. */
function CostSplit({ item }: { item: WorkItem }) {
  if (item.directCost <= 0) return null;
  const shares = TYPES.map((type) => {
    const cost = item.lines.filter((l) => l.resourceType === type).reduce((sum, l) => sum + l.lineCost, 0);
    return { type, percent: Math.round((cost / item.directCost) * 100) };
  }).filter((s) => s.percent > 0);

  return (
    <div className="split">
      <div className="split__bar" aria-hidden="true">
        {shares.map((s) => (
          <span key={s.type} className={`split__seg split__seg--${s.type.toLowerCase()}`} style={{ flexGrow: s.percent }} />
        ))}
      </div>
      <p className="split__legend">
        {shares.map((s) => (
          <span key={s.type}>
            <i className={`dot split__seg--${s.type.toLowerCase()}`} aria-hidden="true" />
            {resourceTypeLabel[s.type]} {s.percent}%
          </span>
        ))}
      </p>
    </div>
  );
}
