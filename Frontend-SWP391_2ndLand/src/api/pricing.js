import api from './axios';

/**
 * Call backend pricing suggestion endpoint.
 * @param {object} payload Listing data (subset) used for AI pricing.
 */
export async function fetchServerPriceSuggestion(payload) {
  const { data } = await api.post('/pricing/suggest', payload);
  // Pass through extended fields if backend provides them.
  return {
    suggestedPrice: data.suggestedPrice,
    reason: data.reason,
    model: data.model,
    mode: data.mode,
    heuristicPrice: data.heuristicPrice ?? data.baseline,
    minPrice: data.minPrice,
    maxPrice: data.maxPrice,
    deltaPercent: data.deltaPercent,
    confidence: data.confidence,
    cacheHit: data.cacheHit,
    clamped: data.clamped,
    promptVersion: data.promptVersion,
    baselinePrice: data.baselinePrice,
    clampPercent: data.clampPercent,
    factorAge: data.factorAge,
    factorCapacity: data.factorCapacity,
    factorCondition: data.factorCondition,
    factorMileage: data.factorMileage,
    factorHealth: data.factorHealth,
    evidence: data.evidence,
  };
}
