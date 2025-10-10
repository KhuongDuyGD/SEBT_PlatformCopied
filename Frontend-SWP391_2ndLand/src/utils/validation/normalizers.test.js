import { describe, it, expect } from 'vitest';
import { normalizeBrand, normalizeModel, safeNumber, clampPercent, buildAutoTitle, sanitizeListingDraft } from './normalizers';

describe('normalizers utilities', () => {
  it('normalizeBrand title-cases or uppercases short tokens', () => {
    expect(normalizeBrand('  vinfast vF8  ')).toBe('Vinfast VF8');
    expect(normalizeBrand(' bmw i3  ')).toBe('BMW I3');
  });

  it('normalizeModel trims & collapses spaces', () => {
    expect(normalizeModel(' Model   S ')).toBe('Model S');
  });

  it('safeNumber parses and bounds', () => {
    expect(safeNumber('10', { min: 5 }).value).toBe(10);
    expect(safeNumber('2', { min: 5 }).error).toBe('below-min');
    expect(safeNumber('abc').error).toBe('NaN');
  });

  it('clampPercent clamps correctly', () => {
    expect(clampPercent(150)).toBe(100);
    expect(clampPercent(-5)).toBe(0);
    expect(clampPercent(55)).toBe(55);
  });

  it('buildAutoTitle for vehicle', () => {
    const title = buildAutoTitle({ productType: 'VEHICLE', vehicle: { brand: 'vinfast', model: 'vf8', year: 2023 } });
    expect(title).toContain('Vinfast');
    expect(title.toLowerCase()).toContain('vf8');
  });

  it('sanitizeListingDraft fills short title', () => {
    const draft = {
      title: 'xe',
      productType: 'VEHICLE',
      vehicle: { brand: 'vinfast', model: 'vf9', name: 'vf9', year: 2024 },
      battery: null
    };
    const sanitized = sanitizeListingDraft(draft);
    expect(sanitized.title.length).toBeGreaterThanOrEqual(5);
  });
});
