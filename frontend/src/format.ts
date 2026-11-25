import type { ResourceType } from './api/client';

const ariary = new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 });
const quantity = new Intl.NumberFormat('en-US', { maximumFractionDigits: 4 });

export const formatAriary = (amount: number) => `${ariary.format(amount)} Ar`;
export const formatQuantity = (value: number) => quantity.format(value);

export const resourceTypeLabel: Record<ResourceType, string> = {
  MATERIAL: 'Material',
  LABOUR: 'Labour',
  EQUIPMENT: 'Equipment',
};
