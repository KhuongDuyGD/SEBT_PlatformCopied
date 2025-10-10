// priceEstimation.js
// Core heuristic price estimation & Gemini prompt builder for EV / Battery listings.
// This provides:
//  - buildGeminiPricingPrompt(listingData)
//  - heuristicSuggestPrice(listingData) (fallback/offline estimation)
//  - suggestPriceWithGemini(listingData, { apiKey }) wrapper (optional remote call)
//
// NOTE: Do NOT place secrets in code. Pass API key via VITE_GEMINI_API_KEY or function param.

import axios from 'axios';

// --------- CONFIG / REFERENCE DATA (heuristic) ---------
// Approximate new-price baselines (VND) for popular Vietnamese EV / battery products.
// These are rough reference anchors; real market data should eventually replace this.
const NEW_PRICE_BASELINES = {
  // VinFast scooters
  'vinfast:feliz s': 29900000,
  'vinfast:feliz': 27900000,
  'vinfast:klara s': 36900000,
  'vinfast:evo 200': 22500000,
  'yamaha:neu': 20000000, // placeholder examples
  // Generic fallback buckets
  '__EV_DEFAULT__': 25000000,
  '__BAT_DEFAULT__': 5000000
};

// Depreciation parameters
const YEAR_DEPRECIATION_FIRST = 0.15;  // 15% first year
const YEAR_DEPRECIATION_NEXT = 0.10;   // 10% each subsequent year
const MAX_DEPRECIATION_FACTOR = 0.75;  // Cap total depreciation at 75% (value floor 25%)

// Condition impact (multipliers relative to computed base after depreciation)
const CONDITION_FACTORS = {
  NEW: 1.00,
  EXCELLENT: 0.97,
  GOOD: 0.93,
  FAIR: 0.85,
  POOR: 0.70,
  NEEDS_MAINTENANCE: 0.65,
  // User-provided generic term mapping
  Used: 0.90,
  used: 0.90
};

// Battery health factor adjustment (centered at 90%; each +/-1% health adjusts 0.5%)
function batteryHealthFactor(healthPct) {
  if (healthPct == null || isNaN(healthPct)) return 1;
  const delta = healthPct - 90; // baseline 90%
  return 1 + (delta * 0.005); // 0.5% per percentage point
}

// Utility: normalize brand/model key
function keyForBrandModel(brand, model) {
  if (!brand) return null;
  const b = String(brand).trim().toLowerCase();
  const m = model ? String(model).trim().toLowerCase() : '';
  return `${b}:${m}`.trim();
}

// Extract numeric kWh from a variety of inputs: "3.5", "3.5 kWh", 3.5
function parseKWh(value) {
  if (value == null) return null;
  if (typeof value === 'number') return value;
  const match = String(value).replace(',', '.').match(/([0-9]+(?:\.[0-9]+)?)/);
  return match ? parseFloat(match[1]) : null;
}

// Heuristic depreciation factor by age
function depreciationFactor(year) {
  const nowYear = new Date().getFullYear();
  if (!year || year > nowYear + 1) return 0.50; // suspicious => assume heavy depreciation
  const age = Math.max(0, nowYear - year);
  if (age === 0) return 1.0 - 0.05; // brand new but "listing" => slight initial drop 5%
  let remaining = 1.0;
  for (let i = 0; i < age; i++) {
    if (i === 0) remaining *= (1 - YEAR_DEPRECIATION_FIRST);
    else remaining *= (1 - YEAR_DEPRECIATION_NEXT);
  }
  const minRemaining = 1 - MAX_DEPRECIATION_FACTOR; // floor
  return Math.max(remaining, minRemaining);
}

// Primary heuristic estimation (returns integer VND) or null
export function heuristicSuggestPrice(listingData) {
  if (!listingData || typeof listingData !== 'object') return null;
  const category = listingData.category || listingData.listingType || ''; // EV or BATTERY

  // Determine brand/model (EV path)
  const product = listingData.product || {};
  const brand = product.brand || product?.ev?.brand || product?.battery?.brand;
  const model = product.model || product?.ev?.model || product?.battery?.model;
  const conditionRaw = product.condition || product.conditionStatus || product?.ev?.conditionStatus || product?.battery?.conditionStatus || listingData.condition;
  const condition = conditionRaw && CONDITION_FACTORS[conditionRaw] ? conditionRaw : 'GOOD';
  const year = product.year || product?.ev?.year || product?.battery?.year;

  // Battery / health info (try both EV and battery branches)
  const batteryCap = parseKWh(product.batteryCapacity || product?.ev?.batteryCapacity || product?.battery?.capacity);
  const healthPct = product.healthPercentage || product?.battery?.healthPercentage || null;

  // Baseline new price
  let baseKey = keyForBrandModel(brand, model);
  let newPrice = (baseKey && NEW_PRICE_BASELINES[baseKey]) || null;
  if (!newPrice) {
    if ((category || '').toUpperCase() === 'EV') newPrice = NEW_PRICE_BASELINES['__EV_DEFAULT__'];
    else if ((category || '').toUpperCase() === 'BATTERY') newPrice = NEW_PRICE_BASELINES['__BAT_DEFAULT__'];
    else newPrice = 20000000; // generic fallback
  }

  // Apply depreciation:
  const yearFactor = depreciationFactor(year);
  // Condition factor
  const conditionFactor = CONDITION_FACTORS[condition] || 0.90;
  // Battery health factor
  const bhFactor = batteryHealthFactor(healthPct);

  // Battery capacity scaling (EV: moderate influence; battery-only: stronger influence)
  let capacityFactor = 1;
  if (batteryCap) {
    if ((category || '').toUpperCase() === 'EV') {
      // Typical scooter pack 2 – 4 kWh; adjust within a narrow band
      // baseline 3 kWh
      capacityFactor = 1 + ((batteryCap - 3) * 0.04); // ~4% per kWh delta
    } else if ((category || '').toUpperCase() === 'BATTERY') {
      // baseline 3 kWh for removable battery
      capacityFactor = 1 + ((batteryCap - 3) * 0.08); // 8% per kWh delta
    }
  }

  let estimated = newPrice * yearFactor * conditionFactor * bhFactor * capacityFactor;

  // Clamp to typical ranges if EV / Battery so it stays realistic
  if ((category || '').toUpperCase() === 'EV') {
    estimated = Math.min(Math.max(estimated, 10000000), 35000000);
  } else if ((category || '').toUpperCase() === 'BATTERY') {
    estimated = Math.min(Math.max(estimated, 2000000), 10000000);
  }

  // Round to nearest 100,000 VND for nicer presentation
  estimated = Math.round(estimated / 100000) * 100000;
  return estimated;
}

// Build Gemini prompt according to required template
export function buildGeminiPricingPrompt(listingData) {
  return (
`Hãy đóng vai chuyên gia thẩm định giá xe điện & pin tại Việt Nam.
Phân tích thông tin sau và ước lượng giá hợp lý (đơn vị VNĐ):

${JSON.stringify(listingData, null, 2)}

Trả về:
1. Giá đề xuất (chỉ 1 con số, đơn vị VNĐ)
2. Giải thích ngắn gọn lý do đề xuất (≤ 3 câu)`
  );
}

// Call Gemini API (if apiKey valid). Returns { suggestedPrice, reason, rawModelOutput }
export async function suggestPriceWithGemini(listingData, { apiKey = import.meta?.env?.VITE_GEMINI_API_KEY } = {}) {
  if (!apiKey) {
    return { suggestedPrice: heuristicSuggestPrice(listingData), reason: 'Dùng heuristic nội bộ (thiếu API key Gemini).', rawModelOutput: null };
  }
  const prompt = buildGeminiPricingPrompt(listingData);
  try {
    const res = await axios.post(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`,
      { contents: [{ parts: [{ text: prompt }] }] },
      { headers: { 'Content-Type': 'application/json' } }
    );
    const text = res.data?.candidates?.[0]?.content?.parts?.[0]?.text || '';
    // Attempt to extract JSON-like answer (robust fallback)
    const priceMatch = text.match(/([0-9]{2,})/);
    const suggested = priceMatch ? parseInt(priceMatch[1], 10) : heuristicSuggestPrice(listingData);
    return { suggestedPrice: suggested, reason: 'Mô hình Gemini gợi ý (đã chuẩn hoá).', rawModelOutput: text };
  } catch (e) {
    console.warn('Gemini call failed, fallback heuristic.', e?.message);
    return { suggestedPrice: heuristicSuggestPrice(listingData), reason: 'Fallback heuristic do lỗi gọi Gemini.', rawModelOutput: null };
  }
}

// High-level helper producing final response JSON per spec.
export function buildPriceSuggestionJSON(listingData) {
  const est = heuristicSuggestPrice(listingData);
  if (!est) {
    return {
      suggestedPrice: null,
      reason: 'Thiếu dữ liệu để ước lượng chính xác (cần thêm thông tin về thương hiệu, năm hoặc dung lượng pin).'
    };
  }
  return {
    suggestedPrice: est,
    reason: 'Ước lượng heuristic dựa trên thương hiệu, năm, tình trạng và dung lượng pin tham chiếu thị trường Việt Nam.'
  };
}

export default {
  heuristicSuggestPrice,
  buildGeminiPricingPrompt,
  suggestPriceWithGemini,
  buildPriceSuggestionJSON
};
