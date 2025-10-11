package project.swp.spring.sebt_platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.swp.spring.sebt_platform.dto.request.PricingSuggestRequestDTO;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;
import project.swp.spring.sebt_platform.service.PricingService;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceService;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceService.LookupResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PricingServiceImpl implements PricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);

    @Value("${app.ai.gemini.apiKey:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    // New configurable knobs
    @Value("${app.pricing.clamp.percent:0.15}")
    private double clampPercent; // e.g. 0.15 = ±15%
    @Value("${app.pricing.retry.attempts:3}")
    private int retryAttempts; // total attempts including first
    @Value("${app.pricing.retry.initialDelayMillis:400}")
    private long initialDelayMillis; // backoff base
    @Value("${app.pricing.fallback.model:gemini-1.5-flash-latest}")
    private String fallbackModel;
    @Value("${app.pricing.cache.enabled:true}")
    private boolean cacheEnabled;
    @Value("${app.pricing.cache.maxSize:500}")
    private int cacheMaxSize;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com";
    private static final String GEMINI_API_PATH_TEMPLATE = "/v1beta/models/%s:generateContent?key="; // model injected

    private final BaselinePriceService baselinePriceService;

    public PricingServiceImpl(BaselinePriceService baselinePriceService) {
        this.baselinePriceService = baselinePriceService;
    }

    @Override
    public PricingSuggestResponseDTO suggestPrice(PricingSuggestRequestDTO request) {
        // Simple cache key (normalize maps): brand|model|year|capacity|condition|health|mileage|province|district|category
        String cacheKey = buildCacheKey(request);
        PricingSuggestResponseDTO cached = CACHE.get(cacheKey);
        if (cached != null) {
            logger.debug("Pricing cache hit key={}", cacheKey);
            cached.setCacheHit(true);
            return cached;
        }

        HeuristicResult heur = heuristicSuggestImproved(request);
        Long heuristic = heur.heuristicRounded;
        double dynamicClamp = heur.dynamicClampPercent; // already computed (0.xx)
        double pct = dynamicClamp;
        Long min = heuristic == null ? null : Math.round(heuristic * (1 - pct) / 1000.0) * 1000L;
        Long max = heuristic == null ? null : Math.round(heuristic * (1 + pct) / 1000.0) * 1000L;
    final String promptVersion = "v3"; // upgraded prompt

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            logger.warn("Gemini API key not configured (app.ai.gemini.apiKey / GEMINI_API_KEY). Using heuristic fallback only.");
        PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, promptVersion,
            "Heuristic (không dùng AI)");
        enrichBreakdown(resp, heur, pct);
            resp.setPrompt(null);
            resp.setCacheHit(false);
            CACHE.put(cacheKey, resp);
            return resp;
        }
        String primaryModel = (geminiModel == null || geminiModel.isBlank()) ? "gemini-2.5-flash" : geminiModel.trim();
        String modelInUse = primaryModel;
    String prompt = buildPromptV3(request, heuristic, min, max, promptVersion, heur, pct);

        int attempts = 0;
        while (true) {
            attempts++;
            try {
                String endpoint = GEMINI_BASE + String.format(GEMINI_API_PATH_TEMPLATE, modelInUse) + geminiApiKey;
                logger.debug("Calling Gemini attempt={} model='{}' endpoint='{}'", attempts, modelInUse, endpoint.replace(geminiApiKey, "***"));
                Map<String, Object> body = Map.of(
                        "contents", List.of(Map.of(
                                "parts", List.of(Map.of("text", prompt))
                        ))
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);

                PricingSuggestResponseDTO result;
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    result = postProcessGeminiResponse(response.getBody(), modelInUse, heuristic, min, max, promptVersion, prompt, heur, pct);
                    enrichBreakdown(result, heur, pct);
                } else {
                    String code = String.valueOf(response.getStatusCode().value());
                    logger.warn("Gemini non-2xx attempt={} status={} snippet={}", attempts, code, truncate(response.getBody(), 160));
                    if (shouldRetry(response.getStatusCode().value(), attempts)) {
                        sleepBackoff(attempts);
                        // đổi sang fallback model nếu primary lỗi liên tục mã 503 / 500
                        if ((response.getStatusCode().value() == 503 || response.getStatusCode().value() == 500) && fallbackModel != null && !fallbackModel.isBlank()) {
                            modelInUse = fallbackModel;
                        }
                        continue;
                    }
            result = baseResponseFromHeuristic(heuristic, min, max, promptVersion,
                "Fallback heuristic HTTP=" + response.getStatusCode().value());
            enrichBreakdown(result, heur, pct);
                }
                result.setPrompt(truncate(prompt, 4000));
                result.setCacheHit(false);
                putCache(cacheKey, result);
                logStructured(result, cacheKey, attempts, modelInUse);
                return result;
            } catch (HttpStatusCodeException httpEx) {
                int code = httpEx.getStatusCode().value();
                logger.warn("Gemini HTTP ex attempt={} status={} snippet={}", attempts, code, truncate(httpEx.getResponseBodyAsString(), 160));
                if (shouldRetry(code, attempts)) {
                    sleepBackoff(attempts);
                    if ((code == 503 || code == 500) && fallbackModel != null && !fallbackModel.isBlank()) {
                        modelInUse = fallbackModel;
                    }
                    continue;
                }
                String userReason;
                if (code == 503) userReason = "Dịch vụ AI tạm thời quá tải (503) → dùng heuristic";
                else if (code == 429) userReason = "AI quota / rate limit (429) → dùng heuristic";
                else userReason = "Fallback heuristic after HTTP error: " + code;
        PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, promptVersion,
            userReason);
        enrichBreakdown(resp, heur, pct);
                resp.setPrompt(truncate(prompt, 4000));
                resp.setCacheHit(false);
                putCache(cacheKey, resp);
                logStructured(resp, cacheKey, attempts, modelInUse);
                return resp;
            } catch (Exception e) {
                logger.error("Gemini exception attempt={} type={} msg={}", attempts, e.getClass().getSimpleName(), e.getMessage());
                if (attempts < retryAttempts) {
                    sleepBackoff(attempts);
                    continue;
                }
        PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, promptVersion,
            "Fallback exception=" + e.getClass().getSimpleName());
        enrichBreakdown(resp, heur, pct);
                resp.setPrompt(truncate(prompt, 4000));
                resp.setCacheHit(false);
                putCache(cacheKey, resp);
                logStructured(resp, cacheKey, attempts, modelInUse);
                return resp;
            }
        }
    }

    private String truncate(String s, int max) { return s == null ? null : (s.length() <= max ? s : s.substring(0, max) + "..."); }

    private PricingSuggestResponseDTO postProcessGeminiResponse(String body, String model, Long heuristic, Long min, Long max, String promptVersion, String prompt,
                                                                HeuristicResult heur, double pct) {
        boolean attemptedSemanticRetry = false;
        String workingBody = body;
    for (int semanticAttempt = 0; semanticAttempt < 2; semanticAttempt++) {
            try {
                project.swp.spring.sebt_platform.pricing.ai.GeminiResponseParser parser = new project.swp.spring.sebt_platform.pricing.ai.GeminiResponseParser();
                var result = parser.parse(workingBody);
                if (result.price() != null && result.price() > 0) {
                    PricingSuggestResponseDTO dto = enrichedFinalFromAi(result.price(), Optional.ofNullable(result.reasoning()).orElse("AI JSON parsed"), model, heuristic, min, max, promptVersion, prompt, heur, pct);
                    if (dto.getEvidence() == null) dto.setEvidence(new ArrayList<>());
                    for (String ev : result.evidence()) if (!dto.getEvidence().contains(ev)) dto.getEvidence().add(ev);
                    // Range validation for clamp evidence
                    if (dto.getSuggestedPrice() != null && min != null && max != null) {
                        long sp = dto.getSuggestedPrice();
                        if ((sp < min || sp > max) && !dto.getEvidence().contains("clamp")) dto.getEvidence().add("clamp");
                    }
                    enrichBreakdown(dto, heur, pct);
                    return dto;
                }
                if (result.numberFallback()) {
                    PricingSuggestResponseDTO dto = enrichedFinalFromAi(result.price(), result.reasoning(), model, heuristic, min, max, promptVersion, prompt, heur, pct);
                    enrichBreakdown(dto, heur, pct);
                    return dto;
                }
                // else proceed to semantic retry block below
            } catch (Exception e) {
                logger.error("Parse Gemini response error: {}", e.getMessage());
            }
            if (!attemptedSemanticRetry) {
                attemptedSemanticRetry = true;
                // Build corrective mini prompt: instruct model to ONLY output JSON with required fields.
                String corrective = prompt + "\nCHỈ TRẢ VỀ JSON HỢP LỆ duy nhất dạng: {\\\"suggestedPrice\\\": <int>, \\\"reasoning\\\": \\\"<=2 câu\\\", \\\"evidence\\\":[...]}";
                try {
                    String endpoint = GEMINI_BASE + String.format(GEMINI_API_PATH_TEMPLATE, model) + geminiApiKey;
                    Map<String, Object> bodyMap = Map.of(
                            "contents", List.of(Map.of(
                                    "parts", List.of(Map.of("text", corrective))
                            ))
                    );
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyMap, headers);
                    ResponseEntity<String> resp = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
                    if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                        workingBody = resp.getBody();
                        continue; // retry parse loop
                    }
                } catch (Exception inner) {
                    logger.warn("Semantic retry failed: {}", inner.getMessage());
                }
            }
        }
        PricingSuggestResponseDTO dto = enrichedFinalFromAi(null, "Unable to parse Gemini response", model, heuristic, min, max, promptVersion, prompt, heur, pct);
        enrichBreakdown(dto, heur, pct);
        return dto;
    }

    // (legacy helper methods removed; parsing now delegated to GeminiResponseParser)

    private String buildPromptV3(PricingSuggestRequestDTO req, Long heuristic, Long min, Long max, String version, HeuristicResult heur, double clampPct) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROMPT_VERSION=").append(version).append('\n');
        sb.append("ROLE: Bạn là chuyên gia thẩm định giá xe điện & pin tại Việt Nam.\n");
        sb.append("OUTPUT: Chỉ trả về 1 dòng JSON hợp lệ dạng: {\"suggestedPrice\": <int>, \"reasoning\": \"<=2 câu giải thích chênh lệch so với heuristic\", \"evidence\": [\"tag1\",\"tag2\",...] }\n");
        sb.append("evidence tags hợp lệ (chỉ dùng những tag này, chọn 2-6 tag mô tả logic): baseline,depreciation,capacity,mileage,condition,health,market,adjustment,clamp.\n");
        sb.append("KHÔNG thêm text khác. Không định dạng số bằng dấu chấm. Không giải thích ngoài JSON.\n");
        if (heuristic != null) {
            sb.append("HEURISTIC_RESULT=").append(heuristic).append('\n');
            sb.append("ALLOWED_RANGE=[").append(min).append(',').append(max).append("]\n");
            if (heur != null) {
                sb.append("BASELINE_NEW=").append(heur.baseline).append('\n');
                if (heur.strategyType != null) {
                    sb.append("DEPRECIATION_STRATEGY=").append(heur.strategyType)
                      .append("(rate=").append(heur.strategyRate).append(",maxDep=").append(heur.strategyMaxDep).append(")\n");
                }
                sb.append("FACTORS: ").append(heur.factorSummary).append(" clampPercent=")
                  .append(String.format(java.util.Locale.US, "%.2f", clampPct*100)).append('%').append('\n');
            }
            sb.append("Nếu vượt ngoài range hãy giữ trong range và giải thích ngắn gọn.\n");
        }
        sb.append("DỮ LIỆU:\n");
        sb.append("category=").append(req.getCategory()).append('\n');
        if (req.getProduct() != null) req.getProduct().forEach((k,v) -> sb.append("product.").append(k).append('=').append(v).append('\n'));
        if (req.getLocation() != null) req.getLocation().forEach((k,v) -> sb.append("location.").append(k).append('=').append(v).append('\n'));
        sb.append("title=").append(req.getTitle()).append('\n');
        sb.append("description=").append(req.getDescription()).append('\n');
        return sb.toString();
    }

    // ===== Improved heuristic with breakdown & dynamic clamp =====
    private HeuristicResult heuristicSuggestImproved(PricingSuggestRequestDTO req) {
        HeuristicResult r = new HeuristicResult();
        try {
            Map<String,Object> p = req.getProduct();
            if (p == null) return r; // empty result (null values)
            String brand = optString(p.get("brand"));
            String model = optString(p.get("model"));
            Integer year = optInt(p.get("year"));
            String variant = optString(p.get("variant"));
            Double batteryCapacity = optDouble(p.get("batteryCapacity"));
            String condition = optString(p.getOrDefault("condition", "GOOD"));
            Integer mileage = optInt(p.get("mileage"));
            Double health = optDouble(p.get("healthPercentage"));

            // Inference: if model is null/blank but title contains known tokens, derive it
            if ((model == null || model.isBlank()) && req.getTitle() != null) {
                String t = req.getTitle().toLowerCase();
                if (t.contains("vf8")) model = "VF 8"; // canonical spacing
                else if (t.contains("vf9")) model = "VF 9";
                else if (t.contains("vf 8")) model = "VF 8";
                else if (t.contains("vf 9")) model = "VF 9";
            }

            // Attempt data-driven baseline lookup
            int currentYear = java.time.Year.now().getValue();
            int ageYears = (year == null || year > currentYear) ? 0 : Math.max(0, currentYear - year);
            LookupResult lr = baselinePriceService.lookup(brand, model, variant, ageYears);

            double base;
            double ageFactor = 1.0; // if baseline service handles depreciation, skip local age factor
            if (lr.found()) {
                r.baseline = lr.entry().getBaselinePrice();
                base = lr.depreciatedPrice();
                ageFactor = 1.0; // already applied
                if (lr.strategy() != null) {
                    r.strategyType = lr.strategy().getType().name();
                    r.strategyRate = lr.strategy().getRate();
                    r.strategyMaxDep = lr.strategy().getMaxDepreciation();
                }
            } else {
                // fallback legacy baseline heuristic
                base = 300_000_000; // generic EV car fallback
                if (brand != null && brand.equalsIgnoreCase("VinFast")) base = 350_000_000;
                if (model != null) {
                    String m = model.toLowerCase();
                    String composite = ((brand==null?"":brand.toLowerCase()+" ") + m);
                    boolean vf8Plus = composite.contains("vf8 plus") || (composite.contains("vf8") && m.contains("plus"));
                    if (!vf8Plus && brand != null && brand.equalsIgnoreCase("VinFast") && m.contains("plus") && batteryCapacity != null && batteryCapacity >= 70) {
                        vf8Plus = true;
                    }
                    if (vf8Plus) base = 1_050_000_000;
                    else if (m.contains("vf8")) base = 900_000_000;
                    else if (m.contains("vf9")) base = 1_300_000_000;
                    else if (m.contains("e34")) base = 330_000_000;
                    else if (m.contains("feliz")) base = 27_000_000;
                    else if (m.contains("klara")) base = 35_000_000;
                }
                r.baseline = Math.round(base);
                if (ageYears > 0) {
                    ageFactor = Math.pow(0.92, Math.min(10, ageYears));
                }
            }

            // Age factor now either 1.0 (data baseline) or computed legacy fallback

            // Capacity factor (car style). Only reward moderate; cap +30%
            double capacityFactor = 1.0;
            if (batteryCapacity != null) {
                // reference 50 kWh baseline, incremental 0.5% per kWh above 50, capped 30%
                double extra = batteryCapacity - 50.0;
                if (extra > 0) capacityFactor = 1.0 + Math.min(0.30, extra * 0.005);
            }

            // Condition factor map
            double conditionFactor = 1.0;
            if (condition != null) {
                switch (condition.toUpperCase()) {
                    case "EXCELLENT": conditionFactor = 1.00; break;
                    case "GOOD": conditionFactor = 0.99; break; // giảm phạt xe trạng thái tốt để tránh tụt giá bất hợp lý khi gần như mới
                    case "FAIR": conditionFactor = 0.90; break;
                    case "POOR": conditionFactor = 0.80; break;
                    case "NEEDS_MAINTENANCE": conditionFactor = 0.70; break;
                }
            }

            // Mileage factor: up to 50% reduction at 120k km
            double mileageFactor = 1.0;
            if (mileage != null) {
                double ratio = Math.min(1.0, mileage / 120_000.0);
                mileageFactor = 1.0 - 0.50 * ratio;
            }

            // Health factor: linear 0.7..1.0 for 0..100%
            double healthFactor = 1.0;
            if (health != null && health > 0) {
                double h = Math.max(0, Math.min(100, health));
                healthFactor = 0.7 + 0.3 * (h / 100.0);
            }

            double raw = base * ageFactor * capacityFactor * conditionFactor * mileageFactor * healthFactor;

            // Near-new bonus: age 0 && mileage < 10k && condition GOOD/EXCELLENT -> +1.5%
            if (ageYears == 0 && (mileage == null || mileage < 10_000) && ("GOOD".equalsIgnoreCase(condition) || "EXCELLENT".equalsIgnoreCase(condition))) {
                raw *= 1.015;
            }
            raw = Math.max(1_000_000, Math.min(raw, 5_000_000_000L));
            long rounded = Math.round(raw / 1000.0) * 1000L;

            r.heuristicRounded = rounded;

            // Dynamic clamp: base 12%; adjust ± based on data completeness & model specificity
            int missing = 0; // brand/model/year/capacity/health considered
            if (brand == null) missing++;
            if (model == null) missing++;
            if (year == null) missing++;
            if (batteryCapacity == null) missing++;
            if (health == null) missing++;
            double completeness = 1.0 - (missing / 5.0);
            double baseClamp = 0.12;
            // If we matched a specific model baseline (e.g. vf8 plus) tighten by 2%
            boolean specificModel = model != null && (model.toLowerCase().contains("vf8") || model.toLowerCase().contains("vf9") || model.toLowerCase().contains("e34"));
            double adj = specificModel ? -0.02 : 0.0;
            double spreadAdj = (1 - completeness) * 0.05; // up to +5% if missing many fields
            r.dynamicClampPercent = Math.max(0.07, Math.min(0.18, baseClamp + adj + spreadAdj));

            r.factorSummary = String.format("age=%.3f,cap=%.3f,cond=%.3f,km=%.3f,health=%.3f", ageFactor, capacityFactor, conditionFactor, mileageFactor, healthFactor);
            return r;
        } catch (Exception e) {
            // Swallow but we can log at debug to avoid noisy user output
            // log.debug("Heuristic error: {}", e.toString());
            return r;
        }
    }

    private String optString(Object o) { return o == null ? null : String.valueOf(o); }
    private Integer optInt(Object o) { try { if (o == null) return null; return Integer.parseInt(String.valueOf(o).replaceAll("[^0-9]","")); } catch (Exception e){ return null; } }
    private Double optDouble(Object o) { try { if (o == null) return null; return Double.parseDouble(String.valueOf(o).replaceAll("[^0-9.]","")); } catch (Exception e){ return null; } }

    private Long roundToThousand(Double d) { if (d == null) return null; long v = Math.round(d); return Math.round(v / 1000.0) * 1000L; }

    // ===== Added helpers for enriched response =====
    private static final Map<String, PricingSuggestResponseDTO> CACHE = new ConcurrentHashMap<>();

    private boolean shouldRetry(int statusCode, int attempts) {
        if (attempts >= retryAttempts) return false;
        return statusCode == 503 || statusCode == 500 || statusCode == 429; // transient
    }

    private void sleepBackoff(int attempts) {
        try {
            long delay = (long) (initialDelayMillis * Math.pow(2, Math.max(0, attempts - 1))); // exp backoff
            Thread.sleep(Math.min(delay, 4000));
        } catch (InterruptedException ignored) {}
    }

    private void putCache(String key, PricingSuggestResponseDTO value) {
        if (!cacheEnabled) return;
        if (CACHE.size() >= cacheMaxSize) {
            // simple eviction: clear half (could be improved to LRU but fine for now)
            int removeCount = CACHE.size() / 2;
            Iterator<String> it = CACHE.keySet().iterator();
            while (removeCount-- > 0 && it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        CACHE.put(key, value);
    }

    private String buildCacheKey(PricingSuggestRequestDTO req) {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.toString(req.getCategory(), "")).append('|');
        Map<String,Object> p = req.getProduct();
        if (p != null) {
            sb.append(Objects.toString(p.get("brand"), "")) .append('|')
              .append(Objects.toString(p.get("model"), "")) .append('|')
              .append(Objects.toString(p.get("year"), ""))  .append('|')
              .append(Objects.toString(p.get("batteryCapacity"), "")) .append('|')
              .append(Objects.toString(p.get("condition"), "")) .append('|')
              .append(Objects.toString(p.get("healthPercentage"), "")) .append('|')
              .append(Objects.toString(p.get("mileage"), "")) .append('|');
        }
        Map<String,Object> loc = req.getLocation();
        if (loc != null) {
            sb.append(Objects.toString(loc.get("province"), "")) .append('|')
              .append(Objects.toString(loc.get("district"), ""));
        }
        return sb.toString().toLowerCase();
    }

    private PricingSuggestResponseDTO baseResponseFromHeuristic(Long heuristic, Long min, Long max, String promptVersion, String reason) {
        if (heuristic == null) {
            PricingSuggestResponseDTO dto = new PricingSuggestResponseDTO(null, reason, null, "heuristic", null, min, max, false, null, null, promptVersion);
            dto.setEvidence(new java.util.ArrayList<>(java.util.List.of("heuristic")));
            return dto;
        }
        PricingSuggestResponseDTO dto = new PricingSuggestResponseDTO(heuristic, reason, null, "heuristic", heuristic, min, max, false, 1.0, 0.0, promptVersion);
        dto.setEvidence(new java.util.ArrayList<>(java.util.List.of("heuristic")));
        return dto;
    }

    private PricingSuggestResponseDTO enrichedFinalFromAi(Double aiPriceRaw, String reason, String model, Long heuristic, Long min, Long max,
                                                          String promptVersion, String prompt, HeuristicResult heur, double pct) {
        Long aiRounded = aiPriceRaw == null ? null : roundToThousand(aiPriceRaw);
        boolean clamped = false;
        Long finalPrice = aiRounded;
        if (finalPrice == null && heuristic != null) finalPrice = heuristic;
        // Pre-clamp deviation for confidence calibration
        Double preClampDeviationRatio = null;
        if (heuristic != null && aiRounded != null) {
            preClampDeviationRatio = Math.abs(aiRounded - heuristic) / (double) heuristic; // e.g. 0.12 = 12%
        }
        if (finalPrice != null && min != null && max != null) {
            if (finalPrice < min) { finalPrice = min; clamped = true; }
            else if (finalPrice > max) { finalPrice = max; clamped = true; }
        }
        // Hard cap: không bao giờ vượt baseline gốc (giá xe mới) nếu baseline có sẵn
        boolean baselineCapApplied = false;
        if (finalPrice != null && heur != null && heur.baseline > 0 && finalPrice > heur.baseline) {
            finalPrice = heur.baseline;
            clamped = true;
            baselineCapApplied = true;
        }
        Double deltaPercent = null, confidence = null;
        if (heuristic != null && finalPrice != null) {
            deltaPercent = (finalPrice - heuristic) * 100.0 / heuristic;
            confidence = 1.0 - Math.min(1.0, Math.abs(finalPrice - heuristic) / (double) heuristic);
            // Confidence calibration: if raw AI (pre-clamp) deviated >10%, subtract 0.10 (floor 0.1)
            if (preClampDeviationRatio != null && preClampDeviationRatio > 0.10) {
                confidence = Math.max(0.10, confidence - 0.10);
            }
        }
        PricingSuggestResponseDTO dto = new PricingSuggestResponseDTO(finalPrice, reason, model, aiRounded == null ? "heuristic" : "gemini",
        heuristic, min, max, clamped, confidence, deltaPercent, promptVersion);
        // Seed evidence with baseline/heuristic factors; AI evidence will merge later after parse stage enhancement
        java.util.List<String> ev = new java.util.ArrayList<>();
        ev.add("baseline");
        if (heur.strategyType != null) ev.add("depreciation");
        ev.add("heuristic");
    if (clamped) ev.add("clamp");
        if (baselineCapApplied) ev.add("baseline-cap");
        dto.setEvidence(ev);
        // Tạm reuse 'reason' field cho prompt debug nếu cần – có thể mở rộng sang field mới nếu muốn.
        return dto;
    }

    // Helper struct
    private static class HeuristicResult {
        long baseline;
        long heuristicRounded;
        String factorSummary=""; // e.g. age=...,cap=...,cond=...,km=...,health=...
        double dynamicClampPercent = 0.15; // default
        String strategyType; // LINEAR / EXPONENTIAL / NONE
        Double strategyRate; // numeric rate
        Double strategyMaxDep; // cap fraction
    }

    // (Test hooks removed – logic now validated via reflection or higher-level tests)

    private void enrichBreakdown(PricingSuggestResponseDTO dto, HeuristicResult heur, double clampPct) {
        if (dto == null || heur == null) return;
        dto.setBaselinePrice(heur.baseline);
        dto.setClampPercent(clampPct);
        if (heur.strategyType != null) {
            dto.setStrategyType(heur.strategyType);
            dto.setStrategyRate(heur.strategyRate);
            dto.setStrategyMaxDep(heur.strategyMaxDep);
        }
        // Parse factorSummary back into fields (simple split) for clarity
        try {
            String[] parts = heur.factorSummary.split(",");
            for (String part : parts) {
                String[] kv = part.split("=");
                if (kv.length == 2) {
                    double val = Double.parseDouble(kv[1]);
                    switch (kv[0]) {
                        case "age": dto.setFactorAge(val); break;
                        case "cap": dto.setFactorCapacity(val); break;
                        case "cond": dto.setFactorCondition(val); break;
                        case "km": dto.setFactorMileage(val); break;
                        case "health": dto.setFactorHealth(val); break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void logStructured(PricingSuggestResponseDTO dto, String cacheKey, int attempts, String modelUsed) {
        try {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("ts", System.currentTimeMillis());
            m.put("cacheKey", cacheKey);
            m.put("attempts", attempts);
            m.put("aiModel", modelUsed);
            m.put("mode", dto.getMode());
            m.put("suggestedPrice", dto.getSuggestedPrice());
            m.put("heuristicPrice", dto.getHeuristicPrice());
            m.put("minPrice", dto.getMinPrice());
            m.put("maxPrice", dto.getMaxPrice());
            m.put("clamped", dto.getClamped());
            m.put("deltaPercent", dto.getDeltaPercent());
            m.put("confidence", dto.getConfidence());
            m.put("baselinePrice", dto.getBaselinePrice());
            m.put("strategyType", dto.getStrategyType());
            m.put("strategyRate", dto.getStrategyRate());
            m.put("strategyMaxDep", dto.getStrategyMaxDep());
            m.put("clampPercent", dto.getClampPercent());
            m.put("fAge", dto.getFactorAge());
            m.put("fCap", dto.getFactorCapacity());
            m.put("fCond", dto.getFactorCondition());
            m.put("fKm", dto.getFactorMileage());
            m.put("fHealth", dto.getFactorHealth());
            m.put("evidence", dto.getEvidence());
            m.put("cacheHit", dto.getCacheHit());
            String json = objectMapper.writeValueAsString(m);
            Logger pricingLogger = LoggerFactory.getLogger("PRICING_EVENT");
            pricingLogger.info(json);
        } catch (Exception e) {
            logger.warn("Failed structured pricing log: {}", e.getMessage());
        }
    }
}
