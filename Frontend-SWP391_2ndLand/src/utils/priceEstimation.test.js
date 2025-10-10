import { describe, it, expect } from 'vitest';
import { heuristicSuggestPrice } from './priceEstimation';

describe('heuristicSuggestPrice', () => {
  it('estimates VinFast Feliz S 2023 with good battery', () => {
    const data = {
      title: 'Xe điện VinFast Feliz S 2023',
      category: 'EV',
      product: { brand: 'VinFast', model: 'Feliz S', batteryCapacity: '3.5 kWh', year: 2023, condition: 'Used', healthPercentage: 95 },
      description: 'Pin còn 95%'
    };
    const price = heuristicSuggestPrice(data);
    // Expect within plausible 15–25 triệu
    expect(price).toBeGreaterThanOrEqual(15000000);
    expect(price).toBeLessThanOrEqual(25000000);
  });

  it('returns null on missing object', () => {
    expect(heuristicSuggestPrice(null)).toBeNull();
  });
});
