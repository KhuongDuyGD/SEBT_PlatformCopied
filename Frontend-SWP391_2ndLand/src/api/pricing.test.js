import { describe, it, expect, vi } from 'vitest';
import * as pricingApi from './pricing';
import api from './axios';

vi.mock('./axios', () => ({ default: { post: vi.fn() } }));

describe('pricing api mapping', () => {
  it('maps breakdown fields from response', async () => {
    api.post.mockResolvedValueOnce({ data: {
      suggestedPrice: 100000000,
      reason: 'ok',
      model: 'gemini-test',
      mode: 'gemini',
      heuristicPrice: 95000000,
      minPrice: 90000000,
      maxPrice: 110000000,
      deltaPercent: 5.26,
      confidence: 0.93,
      cacheHit: false,
      clamped: false,
      promptVersion: 'v2',
      baselinePrice: 88000000,
      clampPercent: 0.12,
      factorAge: 0.96,
      factorCapacity: 1.05,
      factorCondition: 0.97,
      factorMileage: 0.90,
      factorHealth: 0.85,
    }});
    const result = await pricingApi.fetchServerPriceSuggestion({});
    expect(result.baselinePrice).toBe(88000000);
    expect(result.clampPercent).toBe(0.12);
    expect(result.factorAge).toBe(0.96);
    expect(result.factorCapacity).toBe(1.05);
    expect(result.factorCondition).toBe(0.97);
    expect(result.factorMileage).toBe(0.90);
    expect(result.factorHealth).toBe(0.85);
  });
});
