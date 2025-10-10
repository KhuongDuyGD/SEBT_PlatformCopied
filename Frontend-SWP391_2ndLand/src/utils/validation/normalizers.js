// Normalization & validation helper utilities for Create Listing flow
// These utilities are intentionally pure & side-effect free so they can be unit tested easily.

/** Normalize brand/manufacturer strings
 * - Trim
 * - Collapse internal whitespace
 * - Uppercase first letter of each token (basic title case, keeps acronyms if already upper)
 */
export function normalizeBrand(raw) {
  if (!raw) return '';
  return raw
    .trim()
    .replace(/\s+/g, ' ')
    .split(' ')
    .map(tok => tok.length <= 3 ? tok.toUpperCase() : (tok[0].toUpperCase() + tok.slice(1).toLowerCase()))
    .join(' ');
}

/** Normalize model: trim, collapse whitespace, keep case except make leading/trailing safe */
export function normalizeModel(raw) {
  if (!raw) return '';
  return raw.trim().replace(/\s+/g, ' ');
}

/** Parse a number safely from mixed input (string/number) with optional bounds.
 * Returns { value, error } (error is a string if invalid).
 */
export function safeNumber(input, { min, max, integer } = {}) {
  if (input === '' || input == null) return { value: undefined };
  const n = typeof input === 'number' ? input : Number(input);
  if (Number.isNaN(n)) return { value: undefined, error: 'NaN' };
  if (integer && !Number.isInteger(n)) return { value: undefined, error: 'not-integer' };
  if (min != null && n < min) return { value: n, error: 'below-min' };
  if (max != null && n > max) return { value: n, error: 'above-max' };
  return { value: n };
}

/** Clamp percent 0..100 (or custom bounds) */
export function clampPercent(v, min = 0, max = 100) {
  if (v == null || v === '') return undefined;
  const n = Number(v);
  if (Number.isNaN(n)) return undefined;
  return Math.min(max, Math.max(min, n));
}

/** Build a suggested title from product details if user left title blank or too short */
export function buildAutoTitle({ productType, vehicle, battery }) {
  if (productType === 'VEHICLE' && vehicle) {
    const bits = [normalizeBrand(vehicle.brand), normalizeModel(vehicle.model || vehicle.name), vehicle.year];
    return bits.filter(Boolean).join(' ').trim();
  }
  if (productType === 'BATTERY' && battery) {
    const bits = [normalizeBrand(battery.brand), normalizeModel(battery.model), battery.capacity ? battery.capacity + 'kWh' : null];
    return bits.filter(Boolean).join(' ').trim();
  }
  return '';
}

/** High-level sanitize listing draft before submitting to backend.
 * Only adjusts local object; does not mutate original (returns new object)
 */
export function sanitizeListingDraft(values) {
  const clone = JSON.parse(JSON.stringify(values));
  if (clone.vehicle) {
    clone.vehicle.brand = normalizeBrand(clone.vehicle.brand);
    clone.vehicle.model = normalizeModel(clone.vehicle.model);
  }
  if (clone.battery) {
    clone.battery.brand = normalizeBrand(clone.battery.brand);
    clone.battery.model = normalizeModel(clone.battery.model);
    const hp = clampPercent(clone.battery.healthPercentage, 1, 100);
    if (hp != null) clone.battery.healthPercentage = hp;
  }
  // Auto title suggestion if too short
  if (!clone.title || clone.title.trim().length < 5) {
    const suggested = buildAutoTitle(clone);
    if (suggested.length >= 5) clone.title = suggested;
  }
  return clone;
}

export default {
  normalizeBrand,
  normalizeModel,
  safeNumber,
  clampPercent,
  buildAutoTitle,
  sanitizeListingDraft
};
