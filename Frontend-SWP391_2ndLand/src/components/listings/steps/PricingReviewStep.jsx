import React from 'react';
import { DollarSign, Sparkles, ClipboardList, Info } from 'lucide-react';
import { formatNumberWithDots, createFormattedInputHandler, formatVnd, formatDeltaPercent, formatConfidence } from '../../../utils/numberFormatting';

/**
 * Step 4: Pricing & Review
 * - Allows AI/heuristic price suggestion
 * - Shows concise summary of entered data for final confirmation
 */
export default function PricingReviewStep({
  formData,
  errors,
  register,
  onChange,
  onSuggestPrice,
  priceSuggesting,
  priceSuggestion,
  applySuggestedPrice,
  togglePromptPreview,
  showPromptPreview
}) {
  const regPrice = register ? register('price') : {};

  const productSummary = formData.productType === 'VEHICLE' ? {
    'Loại': formData.vehicle.type,
    'Hãng': formData.vehicle.brand,
    'Tên xe': formData.vehicle.name,
    'Model': formData.vehicle.model || '—',
    'Năm': formData.vehicle.year,
    'Odo (km)': formData.vehicle.mileage,
    'Pin (kWh)': formData.vehicle.batteryCapacity,
    'Tình trạng': formData.vehicle.conditionStatus
  } : {
    'Hãng pin': formData.battery.brand,
    'Model': formData.battery.model || '—',
    'Dung lượng (kWh)': formData.battery.capacity,
    '% Health': formData.battery.healthPercentage,
    'Tương thích': formData.battery.compatibleVehicles || '—',
    'Tình trạng': formData.battery.conditionStatus
  };

  const locationSummary = {
    'Tỉnh/TP': formData.location.province || '—',
    'Quận/Huyện': formData.location.district || '—'
  };

  return (
    <div>
      <div className="step-header">
        <h2 className="step-header-title">
          <DollarSign className="w-7 h-7 text-emerald-600 mr-3" />
          Giá & Xác nhận cuối
        </h2>
        <p className="step-header-subtitle">Kiểm tra lại thông tin và chọn mức giá hợp lý</p>
      </div>

      <div className="step-content space-y-8">
        {/* Pricing Section */}
        <section className="p-4 border rounded bg-white shadow-sm">
          <div className="flex items-center justify-between mb-2">
              <h3 className="font-semibold text-gray-800 flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-purple-600" />Gợi ý & Giá bán
            </h3>
            {onSuggestPrice && (
              <button
                type="button"
                onClick={onSuggestPrice}
                disabled={priceSuggesting}
                className="text-xs px-3 py-1.5 rounded bg-purple-600 hover:bg-purple-700 text-white disabled:opacity-50"
              >{priceSuggesting ? 'Đang gợi ý...' : 'Gợi ý giá'}</button>
            )}
          </div>
          <div className="mt-2">
            <label className="form-label required">Giá bán mong muốn (VND)</label>
            <div className="input-with-icon mt-1">
              <DollarSign className="input-icon w-5 h-5" />
              <input
                type="text"
                name="price"
                value={formatNumberWithDots(formData.price)}
                onChange={createFormattedInputHandler((e)=>{ onChange(e); regPrice.onChange && regPrice.onChange(e); }, 'price')}
                ref={regPrice.ref}
                className={`form-input with-icon ${errors?.price ? 'input-error' : ''}`}
                placeholder="VD: 27.000.000"
                required
              />
            </div>
            {errors?.price && <p className="field-error">{errors.price.message}</p>}
            <p className="text-[11px] text-gray-500 mt-1">Bạn có thể chỉnh sửa sau khi áp dụng gợi ý.</p>
          </div>

          {/* Suggestion Panel */}
          {priceSuggestion && (
            <div className="mt-4 p-4 border rounded bg-gray-50 space-y-3">
              <div className="flex items-center justify-between">
                <h4 className="font-medium flex items-center gap-1">Gợi ý hệ thống {priceSuggestion.promptVersion && <span className="text-[10px] px-1.5 py-0.5 bg-gray-200 rounded">{priceSuggestion.promptVersion}</span>}</h4>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={applySuggestedPrice}
                    disabled={priceSuggesting || priceSuggestion.suggestedPrice == null}
                    className="px-2 py-1 bg-green-600 text-white text-xs rounded disabled:opacity-50"
                  >Áp dụng</button>
                  <button
                    type="button"
                    onClick={togglePromptPreview}
                    className="px-2 py-1 bg-gray-200 text-gray-700 text-xs rounded hover:bg-gray-300"
                  >{showPromptPreview ? 'Ẩn Prompt' : 'Xem Prompt'}</button>
                </div>
              </div>

              {/* Main price line */}
              <div className="text-sm flex flex-wrap items-center gap-2">
                {priceSuggestion.suggestedPrice != null ? (
                  <>
                    <span>Đề xuất:</span>{' '}
                    <span className="font-mono font-semibold text-indigo-600">{formatVnd(priceSuggestion.suggestedPrice)} ₫</span>
                    {priceSuggestion.mode && (
                      <span className="ml-2 text-[10px] px-2 py-0.5 rounded bg-purple-100 text-purple-700 align-middle">
                        {priceSuggestion.mode === 'gemini' ? (priceSuggestion.model || 'AI') : 'heuristic'}
                      </span>
                    )}
                    {priceSuggestion.cacheHit && (
                      <span className="text-[10px] px-2 py-0.5 rounded bg-blue-100 text-blue-700">cache</span>
                    )}
                    {priceSuggestion.clamped && (
                      <span className="ml-2 text-[10px] px-2 py-0.5 rounded bg-amber-100 text-amber-700 align-middle flex items-center gap-1 inline-flex"><Info className="w-3 h-3"/>clamped</span>
                    )}
                  </>
                ) : <span className="text-red-600">{priceSuggestion.reason}</span>}
              </div>

              {/* Range & stats */}
              {(priceSuggestion.minPrice || priceSuggestion.heuristicPrice) && (
                <div className="text-[11px] text-gray-600 flex flex-wrap gap-x-6 gap-y-1 items-center">
                  {priceSuggestion.heuristicPrice != null && (
                    <span>Heuristic: <strong>{formatVnd(priceSuggestion.heuristicPrice)} ₫</strong></span>
                  )}
                  {priceSuggestion.minPrice != null && priceSuggestion.maxPrice != null && (
                    <span>Khoảng: {formatVnd(priceSuggestion.minPrice)} – {formatVnd(priceSuggestion.maxPrice)} ₫</span>
                  )}
                  {priceSuggestion.deltaPercent != null && (
                    <span>Δ {formatDeltaPercent(priceSuggestion.deltaPercent)}</span>
                  )}
                  {priceSuggestion.confidence != null && (
                    <span>Tin cậy: {formatConfidence(priceSuggestion.confidence)}</span>
                  )}
                  {priceSuggestion.minPrice != null && priceSuggestion.maxPrice != null && priceSuggestion.suggestedPrice != null && (
                    <div className="w-full h-2 bg-gray-200 rounded relative overflow-hidden mt-1">
                      {(() => {
                        const { minPrice, maxPrice, suggestedPrice, heuristicPrice } = priceSuggestion;
                        const span = maxPrice - minPrice;
                        if (!span || span <= 0) return null;
                        const posFinal = Math.min(100, Math.max(0, ((suggestedPrice - minPrice) / span) * 100));
                        const posHeu = heuristicPrice != null ? Math.min(100, Math.max(0, ((heuristicPrice - minPrice) / span) * 100)) : null;
                        return (
                          <>
                            <div className="absolute inset-0 bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50" />
                            {posHeu != null && (
                              <div className="absolute top-0 h-full w-0.5 bg-gray-500" style={{ left: posHeu + '%' }} title="Heuristic" />
                            )}
                            <div className="absolute top-0 h-full w-0.5 bg-indigo-600" style={{ left: posFinal + '%' }} title="Final" />
                          </>
                        );
                      })()}
                    </div>
                  )}
                </div>
              )}

              {/* Reason (trim long) */}
              {priceSuggestion.reason && (
                <p className="text-xs text-gray-500 leading-relaxed whitespace-pre-line">
                  {priceSuggestion.reason.length > 320 ? (
                    <>
                      {priceSuggestion.reason.slice(0,320)}…
                      <button type="button" onClick={togglePromptPreview} className="ml-1 underline">Xem thêm</button>
                    </>
                  ) : priceSuggestion.reason}
                </p>
              )}

              {showPromptPreview && priceSuggestion.prompt && (
                <pre className="mt-2 max-h-60 overflow-auto text-xs bg-white p-3 rounded border border-gray-200 whitespace-pre-wrap">{priceSuggestion.prompt}</pre>
              )}
            </div>
          )}
        </section>

        {/* Review Section */}
        <section className="p-4 border rounded bg-white shadow-sm">
          <h3 className="font-semibold text-gray-800 flex items-center gap-2 mb-3">
            <ClipboardList className="w-5 h-5 text-blue-600" />Tóm tắt thông tin
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
            <div>
              <h4 className="font-medium text-gray-700 mb-2">Sản phẩm</h4>
              <ul className="space-y-1">
                {Object.entries(productSummary).map(([k,v]) => (
                  <li key={k} className="flex justify-between gap-4"><span className="text-gray-500">{k}</span><span className="font-medium text-gray-800 truncate">{String(v)}</span></li>
                ))}
              </ul>
            </div>
            <div>
              <h4 className="font-medium text-gray-700 mb-2">Vị trí</h4>
              <ul className="space-y-1">
                {Object.entries(locationSummary).map(([k,v]) => (
                  <li key={k} className="flex justify-between gap-4"><span className="text-gray-500">{k}</span><span className="font-medium text-gray-800 truncate">{String(v)}</span></li>
                ))}
              </ul>
            </div>
          </div>
          {formData.description && (
            <div className="mt-4">
              <h4 className="font-medium text-gray-700 mb-2">Mô tả</h4>
              <p className="text-gray-700 text-sm leading-relaxed whitespace-pre-wrap">{formData.description}</p>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
