import api from './axios';

/**
 * Call backend pricing suggestion endpoint.
 * @param {object} payload Listing data (subset) used for AI pricing.
 */
export async function fetchServerPriceSuggestion(payload) {
  const { data } = await api.post('/pricing/suggest', payload);
  return data; // { suggestedPrice, reason, model, mode }
}
