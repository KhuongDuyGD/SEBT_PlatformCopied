package project.swp.spring.sebt_platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier; // Dùng nếu có nhiều RestTemplate
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.swp.spring.sebt_platform.config.AiConfig;
import project.swp.spring.sebt_platform.dto.request.PricingSuggestRequestDTO;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;
import project.swp.spring.sebt_platform.pricing.ai.GeminiResponseParser;
import project.swp.spring.sebt_platform.service.PricingService;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceService;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceService.LookupResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai dịch vụ định giá xe điện, kết hợp Heuristic nội bộ (baseline, factors)
 * và gọi API Gemini để tinh chỉnh giá. Bao gồm caching, retry, và logging có cấu trúc.
 */
@Service
public class PricingServiceImpl implements PricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);
    // Logger riêng cho các sự kiện định giá có cấu trúc
    private static final Logger pricingLogger = LoggerFactory.getLogger("PRICING_EVENT");

    @Autowired
    private AiConfig aiConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com";
    private static final String GEMINI_API_PATH_TEMPLATE = "/v1beta/models/%s:generateContent?key="; // model injected

    private final BaselinePriceService baselinePriceService;
    // Cache đơn giản trong bộ nhớ
    private static final Map<String, PricingSuggestResponseDTO> CACHE = new ConcurrentHashMap<>();
    private static final String PROMPT_VERSION = "v3";

    public PricingServiceImpl(BaselinePriceService baselinePriceService) {
        this.baselinePriceService = baselinePriceService;
    }


    /**
     * Đề xuất mức giá bán cho một chiếc xe điện.
     * Áp dụng Cache -> Heuristic -> AI Call (với Retry/Fallback).
     */
    @Override
    public PricingSuggestResponseDTO suggestPrice(PricingSuggestRequestDTO request) {
        String cacheKey = buildCacheKey(request);
        PricingSuggestResponseDTO cached = CACHE.get(cacheKey);

        // 1. Kiểm tra Cache
        if (cached != null) {
            logger.debug("Pricing cache hit key={}", cacheKey);
            cached.setCacheHit(true);
            return cached;
        }

        // 2. Tính toán Heuristic và Clamp Range
        HeuristicResult heur = heuristicSuggestImproved(request);
        Long heuristic = heur.heuristicRounded;
        double pct = heur.dynamicClampPercent;
        Long min = Math.round(heuristic * (1 - pct) / 1000.0) * 1000L;
        Long max = Math.round(heuristic * (1 + pct) / 1000.0) * 1000L;

        // 3. Fallback khi AI Key không khả dụng
        if (aiConfig.getGeminiApiKey() == null || aiConfig.getGeminiApiKey().isBlank()) {
            logger.warn("Gemini API key not configured. Using heuristic fallback only.");
            PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, PROMPT_VERSION,
                    "Heuristic (không dùng AI)");
            enrichBreakdown(resp, heur, pct);
            resp.setPrompt(null);
            resp.setCacheHit(false);
            CACHE.put(cacheKey, resp);
            logStructured(resp, cacheKey, 0, null); // Log sự kiện heuristic
            return resp;
        }

        // 4. Gọi AI với Retry Loop
        String modelInUse = (aiConfig.getGeminiModel() == null || aiConfig.getGeminiModel().isBlank()) ? "gemini-2.5-flash" : aiConfig.getGeminiModel().trim();
        String prompt = buildPromptV3(request, heuristic, min, max, PROMPT_VERSION, heur, pct);

        int attempts = 0;
        while (true) {
            attempts++;
            try {
                // Thực hiện gọi API
                String endpoint = GEMINI_BASE + String.format(GEMINI_API_PATH_TEMPLATE, modelInUse) + aiConfig.getGeminiApiKey();
                logger.debug("Calling Gemini attempt={} model='{}' endpoint='{}'", attempts, modelInUse, endpoint.replace(aiConfig.getGeminiApiKey(), "***"));
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
                    // Thành công: Xử lý và làm giàu DTO
                    result = postProcessGeminiResponse(response.getBody(), modelInUse, heuristic, min, max, PROMPT_VERSION, prompt, heur, pct);
                    enrichBreakdown(result, heur, pct);
                } else {
                    // API trả về lỗi non-2xx: kiểm tra retry
                    String code = String.valueOf(response.getStatusCode().value());
                    logger.warn("Gemini non-2xx attempt={} status={} snippet={}", attempts, code, truncate(response.getBody(), 160));
                    if (shouldRetry(response.getStatusCode().value(), attempts)) {
                        sleepBackoff(attempts);
                        // Đổi sang fallback model nếu lỗi 503/500 liên tục
                        if ((response.getStatusCode().value() == 503 || response.getStatusCode().value() == 500) && aiConfig.getFallbackModel() != null && !aiConfig.getFallbackModel().isBlank()) {
                            modelInUse = aiConfig.getFallbackModel();
                        }
                        continue; // Retry
                    }
                    // Fallback cuối cùng sau khi hết retry
                    result = baseResponseFromHeuristic(heuristic, min, max, PROMPT_VERSION,
                            "Fallback heuristic HTTP=" + response.getStatusCode().value());
                    enrichBreakdown(result, heur, pct);
                }

                // 5. Hoàn tất và Cache/Log
                result.setPrompt(truncate(prompt, 4000));
                result.setCacheHit(false);
                putCache(cacheKey, result);
                logStructured(result, cacheKey, attempts, modelInUse);
                return result;

            } catch (HttpStatusCodeException httpEx) {
                // Xử lý lỗi HTTP (4xx, 5xx)
                int code = httpEx.getStatusCode().value();
                logger.warn("Gemini HTTP ex attempt={} status={} snippet={}", attempts, code, truncate(httpEx.getResponseBodyAsString(), 160));
                if (shouldRetry(code, attempts)) {
                    sleepBackoff(attempts);
                    if ((code == 503 || code == 500) && aiConfig.getFallbackModel() != null && !aiConfig.getFallbackModel().isBlank()) {
                        modelInUse = aiConfig.getFallbackModel();
                    }
                    continue; // Retry
                }

                // Fallback sau khi hết retry hoặc lỗi không retry
                String userReason;
                if (code == 503) userReason = "Dịch vụ AI tạm thời quá tải (503) → dùng heuristic";
                else if (code == 429) userReason = "AI quota / rate limit (429) → dùng heuristic";
                else userReason = "Fallback heuristic after HTTP error: " + code;
                PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, PROMPT_VERSION, userReason);
                enrichBreakdown(resp, heur, pct);
                resp.setPrompt(truncate(prompt, 4000));
                resp.setCacheHit(false);
                putCache(cacheKey, resp);
                logStructured(resp, cacheKey, attempts, modelInUse);
                return resp;

            } catch (Exception e) {
                // Xử lý các Exception khác (kết nối, parse nội bộ)
                logger.error("Gemini exception attempt={} type={} msg={}", attempts, e.getClass().getSimpleName(), e.getMessage());
                if (attempts < aiConfig.getRetryAttempts()) {
                    sleepBackoff(attempts);
                    continue;
                }
                // Fallback cuối cùng
                PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, PROMPT_VERSION,
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

    private String truncate(String s, int max) {
        return s == null ? null : (s.length() <= max ? s : s.substring(0, max) + "...");
    }

    /**
     * Xử lý phản hồi thành công từ Gemini, bao gồm cả Semantic Retry (thử parse lại).
     */
    private PricingSuggestResponseDTO postProcessGeminiResponse(String body, String model, Long heuristic, Long min, Long max, String promptVersion, String prompt,
                                                                HeuristicResult heur, double pct) {
        boolean attemptedSemanticRetry = false;
        String workingBody = body;
        // Vòng lặp 2 lần: Thử parse body gốc; Thử parse body sau khi Semantic Retry
        for (int semanticAttempt = 0; semanticAttempt < 2; semanticAttempt++) {
            try {
                GeminiResponseParser parser = new GeminiResponseParser();
                var result = parser.parse(workingBody);
                if (result.price() != null && result.price() > 0) {
                    PricingSuggestResponseDTO dto = enrichedFinalFromAi(result.price(), Optional.ofNullable(result.reasoning()).orElse("AI JSON parsed"), model, heuristic, min, max, promptVersion, prompt, heur, pct);

                    // Ghép các evidence tags
                    if (dto.getEvidence() == null) dto.setEvidence(new ArrayList<>());
                    for (String ev : result.evidence()) if (!dto.getEvidence().contains(ev)) dto.getEvidence().add(ev);

                    // Kiểm tra và thêm tag 'clamp' nếu giá cuối bị giới hạn
                    if (dto.getSuggestedPrice() != null && min != null && max != null) {
                        long sp = dto.getSuggestedPrice();
                        if ((sp < min || sp > max) && !dto.getEvidence().contains("clamp"))
                            dto.getEvidence().add("clamp");
                    }
                    enrichBreakdown(dto, heur, pct);
                    return dto;
                }

                // Fallback khi AI chỉ trả về số
                if (result.numberFallback()) {
                    PricingSuggestResponseDTO dto = enrichedFinalFromAi(result.price(), result.reasoning(), model, heuristic, min, max, promptVersion, prompt, heur, pct);
                    enrichBreakdown(dto, heur, pct);
                    return dto;
                }
            } catch (Exception e) {
                logger.error("Parse Gemini response error: {}", e.getMessage());
            }

            // Semantic Retry: Chỉ chạy lần đầu tiên (semanticAttempt == 0)
            if (!attemptedSemanticRetry) {
                attemptedSemanticRetry = true;
                // Prompt chỉnh sửa: yêu cầu CHỈ trả về JSON hợp lệ
                String corrective = prompt + "\nCHỈ TRẢ VỀ JSON HỢP LỆ duy nhất dạng: {\\\"suggestedPrice\\\": <int>, \\\"reasoning\\\": \\\"<=2 câu\\\", \\\"evidence\\\":[...]}";
                try {
                    String endpoint = GEMINI_BASE + String.format(GEMINI_API_PATH_TEMPLATE, model) + aiConfig.getGeminiModel();
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
                        continue; // Thử parse lại
                    }
                } catch (Exception inner) {
                    logger.warn("Semantic retry failed: {}", inner.getMessage());
                }
            }
        }
        // Fallback Heuristic nếu parse thất bại sau 2 lần thử
        PricingSuggestResponseDTO dto = enrichedFinalFromAi(null, "Unable to parse Gemini response", model, heuristic, min, max, promptVersion, prompt, heur, pct);
        enrichBreakdown(dto, heur, pct);
        return dto;
    }

    /**
     * Xây dựng Prompt V3 cho Gemini.
     */
    private String buildPromptV3(PricingSuggestRequestDTO req, Long heuristic, Long min, Long max, String version, HeuristicResult heur, double clampPct) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROMPT_VERSION=").append(version).append('\n');
        sb.append("ROLE: You are an expert in electric vehicle and battery valuation in Vietnam.\n");
        sb.append("OUTPUT: Return only ONE valid JSON line in the format: {\"suggestedPrice\": <int>, \"reasoning\": \"<=2 short sentences explaining the difference from heuristic\", \"evidence\": [\"tag1\",\"tag2\",...] }\n");
        sb.append("Valid evidence tags (only use from this list, select 2–6 tags describing your logic): baseline, depreciation, capacity, mileage, condition, health, market, adjustment, clamp.\n");
        sb.append("DO NOT include any extra text. DO NOT format numbers with dots. DO NOT explain anything outside the JSON.\n");
        if (heuristic != null) {
            sb.append("HEURISTIC_RESULT=").append(heuristic).append('\n');
            sb.append("ALLOWED_RANGE=[").append(min).append(',').append(max).append("]\n");
            if (heur != null) {
                sb.append("BASELINE_NEW=").append(heur.baseline).append('\n');
                if (heur.strategyType != null) {
                    sb.append("DEPRECIATION_STRATEGY=").append(heur.strategyType)
                            .append("(rate=").append(heur.strategyRate).append(",maxDep=").append(heur.strategyMaxDep).append(")\n");
                }
                // Sử dụng các trường double mới
                sb.append(String.format(java.util.Locale.US, "FACTORS: age=%.3f,cap=%.3f,cond=%.3f,km=%.3f,health=%.3f clampPercent=%.2f%%",
                                heur.ageFactor, heur.capacityFactor, heur.conditionFactor, heur.mileageFactor, heur.healthFactor, clampPct * 100))
                        .append('\n');
            }
            sb.append("If the result exceeds the allowed range, keep it within the range and give a short explanation.\n");
            sb.append("Answering by VietNamese.\n");
        }
        sb.append("DỮ LIỆU:\n");
        sb.append("category=").append(req.getCategory()).append('\n');
        if (req.getProduct() != null)
            req.getProduct().forEach((k, v) -> sb.append("product.").append(k).append('=').append(v).append('\n'));
        if (req.getLocation() != null)
            req.getLocation().forEach((k, v) -> sb.append("location.").append(k).append('=').append(v).append('\n'));
        sb.append("title=").append(req.getTitle()).append('\n');
        sb.append("description=").append(req.getDescription()).append('\n');
        return sb.toString();
    }

    // ===== Improved heuristic with breakdown & dynamic clamp (Đã cập nhật HeuristicResult) =====

    private HeuristicResult heuristicSuggestImproved(PricingSuggestRequestDTO req) {
        HeuristicResult r = new HeuristicResult();
        try {
            Map<String, Object> p = req.getProduct();
            if (p == null) return r;
            String brand = optString(p.get("brand"));
            String model = optString(p.get("model"));
            Integer year = optInt(p.get("year"));
            String variant = optString(p.get("variant"));
            Double batteryCapacity = optDouble(p.get("batteryCapacity"));
            String condition = optString(p.getOrDefault("condition", "GOOD"));
            Integer mileage = optInt(p.get("mileage"));
            Double health = optDouble(p.get("healthPercentage"));

            // Inference
            if ((model == null || model.isBlank()) && req.getTitle() != null) {
                String t = req.getTitle().toLowerCase();
                if (t.contains("vf8")) model = "VF 8";
                else if (t.contains("vf9")) model = "VF 9";
                else if (t.contains("vf 8")) model = "VF 8";
                else if (t.contains("vf 9")) model = "VF 9";
            }

            // Baseline Lookup
            int currentYear = java.time.Year.now().getValue();
            int ageYears = (year == null || year > currentYear) ? 0 : Math.max(0, currentYear - year);
            LookupResult lr = baselinePriceService.lookup(brand, model, variant, ageYears);

            double base;
            double ageFactor = 1.0;
            if (lr.found()) {
                r.baseline = lr.entry().getBaselinePrice();
                base = lr.depreciatedPrice();
                ageFactor = 1.0; // đã được áp dụng trong lookup
                if (lr.strategy() != null) {
                    r.strategyType = lr.strategy().type().name();
                    r.strategyRate = lr.strategy().rate();
                    r.strategyMaxDep = lr.strategy().maxDepreciation();
                }
            } else {
                // Fallback legacy heuristic
                base = 300_000_000;
                if (brand != null && brand.equalsIgnoreCase("VinFast")) base = 350_000_000;
                if (model != null) {
                    String m = model.toLowerCase();
                    String composite = ((brand == null ? "" : brand.toLowerCase() + " ") + m);
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
            r.ageFactor = ageFactor; // Lưu nhân tố tuổi thọ

            // Capacity factor
            double capacityFactor = 1.0;
            if (batteryCapacity != null) {
                double extra = batteryCapacity - 50.0;
                if (extra > 0) capacityFactor = 1.0 + Math.min(0.30, extra * 0.005);
            }
            r.capacityFactor = capacityFactor;

            // Condition factor
            double conditionFactor = 1.0;
            if (condition != null) {
                switch (condition.toUpperCase()) {
                    case "EXCELLENT": conditionFactor = 1.00; break;
                    case "GOOD": conditionFactor = 0.99; break;
                    case "FAIR": conditionFactor = 0.90; break;
                    case "POOR": conditionFactor = 0.80; break;
                    case "NEEDS_MAINTENANCE": conditionFactor = 0.70; break;
                }
            }
            r.conditionFactor = conditionFactor;

            // Mileage factor
            double mileageFactor = 1.0;
            if (mileage != null) {
                double ratio = Math.min(1.0, mileage / 120_000.0);
                mileageFactor = 1.0 - 0.50 * ratio;
            }
            r.mileageFactor = mileageFactor;

            // Health factor
            double healthFactor = 1.0;
            if (health != null && health > 0) {
                double h = Math.max(0, Math.min(100, health));
                healthFactor = 0.7 + 0.3 * (h / 100.0);
            }
            r.healthFactor = healthFactor;

            // Tính toán giá thô (raw price)
            double raw = base * ageFactor * capacityFactor * conditionFactor * mileageFactor * healthFactor;

            // Near-new bonus
            if (ageYears == 0 && (mileage == null || mileage < 10_000) && ("GOOD".equalsIgnoreCase(condition) || "EXCELLENT".equalsIgnoreCase(condition))) {
                raw *= 1.015;
            }
            raw = Math.max(1_000_000, Math.min(raw, 5_000_000_000L));
            long rounded = Math.round(raw / 1000.0) * 1000L;

            r.heuristicRounded = rounded;

            // Dynamic clamp calculation
            int missing = 0;
            if (brand == null) missing++;
            if (model == null) missing++;
            if (year == null) missing++;
            if (batteryCapacity == null) missing++;
            if (health == null) missing++;
            double completeness = 1.0 - (missing / 5.0);
            double baseClamp = 0.12;
            boolean specificModel = model != null && (model.toLowerCase().contains("vf8") || model.toLowerCase().contains("vf9") || model.toLowerCase().contains("e34"));
            double adj = specificModel ? -0.02 : 0.0;
            double spreadAdj = (1 - completeness) * 0.05;
            r.dynamicClampPercent = Math.max(0.07, Math.min(0.18, baseClamp + adj + spreadAdj));

            // Đã xóa r.factorSummary
            return r;
        } catch (Exception e) {
            logger.debug("Heuristic error: {}", e.toString());
            return r;
        }
    }

    // --- Các phương thức chuyển đổi và hỗ trợ chung ---

    private String optString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer optInt(Object o) {
        try {
            if (o == null) return null;
            return Integer.parseInt(String.valueOf(o).replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Double optDouble(Object o) {
        try {
            if (o == null) return null;
            return Double.parseDouble(String.valueOf(o).replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Long roundToThousand(Double d) {
        if (d == null) return null;
        long v = Math.round(d);
        return Math.round(v / 1000.0) * 1000L;
    }

    private boolean shouldRetry(int statusCode, int attempts) {
        if (attempts >= aiConfig.getRetryAttempts()) return false;
        return statusCode == 503 || statusCode == 500 || statusCode == 429; // Lỗi tạm thời (transient)
    }

    private void sleepBackoff(int attempts) {
        try {
            long delay = (long) (aiConfig.getInitialDelayMillis() * Math.pow(2, Math.max(0, attempts - 1)));
            Thread.sleep(Math.min(delay, 4000));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt(); // Best practice khi bắt InterruptedException
        }
    }

    private void putCache(String key, PricingSuggestResponseDTO value) {
        if (!aiConfig.isCacheEnabled()) return;
        if (CACHE.size() >= aiConfig.getCacheMaxSize()) {
            // Cơ chế eviction đơn giản
            int removeCount = CACHE.size() / 2;
            Iterator<String> it = CACHE.keySet().iterator();
            while (removeCount-- > 0 && it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        CACHE.put(key, value);
    }

    /**
     * Xây dựng Cache Key duy nhất.
     */
    private String buildCacheKey(PricingSuggestRequestDTO req) {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.toString(req.getCategory(), "")).append('|');
        Map<String, Object> p = req.getProduct();
        if (p != null) {
            sb.append(Objects.toString(p.get("brand"), "")).append('|')
                    .append(Objects.toString(p.get("model"), "")).append('|')
                    .append(Objects.toString(p.get("year"), "")).append('|')
                    .append(Objects.toString(p.get("batteryCapacity"), "")).append('|')
                    .append(Objects.toString(p.get("condition"), "")).append('|')
                    .append(Objects.toString(p.get("healthPercentage"), "")).append('|')
                    .append(Objects.toString(p.get("mileage"), "")).append('|');
        }
        Map<String, Object> loc = req.getLocation();
        if (loc != null) {
            sb.append(Objects.toString(loc.get("province"), "")).append('|')
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
        // ... (Giữ nguyên logic chính của enrichedFinalFromAi) ...
        Long aiRounded = aiPriceRaw == null ? null : roundToThousand(aiPriceRaw);
        boolean clamped = false;
        Long finalPrice = aiRounded;
        if (finalPrice == null && heuristic != null) finalPrice = heuristic;
        // Pre-clamp deviation for confidence calibration
        Double preClampDeviationRatio = null;
        if (heuristic != null && aiRounded != null) {
            preClampDeviationRatio = Math.abs(aiRounded - heuristic) / (double) heuristic;
        }
        if (finalPrice != null && min != null && max != null) {
            if (finalPrice < min) {
                finalPrice = min;
                clamped = true;
            } else if (finalPrice > max) {
                finalPrice = max;
                clamped = true;
            }
        }
        // Hard cap: không bao giờ vượt baseline gốc (giá xe mới)
        boolean baselineCapApplied = false;
        if (finalPrice != null && heur != null && heur.baseline > 0 && finalPrice > heur.baseline) {
            finalPrice = heur.baseline;
            clamped = true;
            baselineCapApplied = true;
        }
        Double deltaPercent = null, confidence = null;
        if (heuristic != null) {
            deltaPercent = (finalPrice - heuristic) * 100.0 / heuristic;
            confidence = 1.0 - Math.min(1.0, Math.abs(finalPrice - heuristic) / (double) heuristic);
            // Confidence calibration
            if (preClampDeviationRatio != null && preClampDeviationRatio > 0.10) {
                confidence = Math.max(0.10, confidence - 0.10);
            }
        }
        PricingSuggestResponseDTO dto = new PricingSuggestResponseDTO(finalPrice, reason, model, aiRounded == null ? "heuristic" : "gemini",
                heuristic, min, max, clamped, confidence, deltaPercent, promptVersion);
        // Seed evidence
        java.util.List<String> ev = new java.util.ArrayList<>();
        ev.add("baseline");
        if (heur.strategyType != null) ev.add("depreciation");
        ev.add("heuristic");
        if (clamped) ev.add("clamp");
        if (baselineCapApplied) ev.add("baseline-cap");
        dto.setEvidence(ev);
        return dto;
    }

    /**
     * Struct chứa kết quả và chi tiết phân tích của Heuristic.
     * Đã loại bỏ factorSummary string, thay bằng các trường double trực tiếp.
     */
    private static class HeuristicResult {
        long baseline;
        long heuristicRounded;
        double dynamicClampPercent = 0.15; // default

        // Các nhân tố (Factors)
        double ageFactor = 1.0;
        double capacityFactor = 1.0;
        double conditionFactor = 1.0;
        double mileageFactor = 1.0;
        double healthFactor = 1.0;

        String strategyType; // LINEAR / EXPONENTIAL / NONE
        Double strategyRate;
        Double strategyMaxDep;
    }

    /**
     * Làm giàu DTO phản hồi với chi tiết phân tích Heuristic.
     * Đã loại bỏ logic parse chuỗi, gán trực tiếp các nhân tố.
     */
    private void enrichBreakdown(PricingSuggestResponseDTO dto, HeuristicResult heur, double clampPct) {
        if (dto == null || heur == null) return;
        dto.setBaselinePrice(heur.baseline);
        dto.setClampPercent(clampPct);
        if (heur.strategyType != null) {
            dto.setStrategyType(heur.strategyType);
            dto.setStrategyRate(heur.strategyRate);
            dto.setStrategyMaxDep(heur.strategyMaxDep);
        }

        // Gán trực tiếp các nhân tố đã được tính toán
        dto.setFactorAge(heur.ageFactor);
        dto.setFactorCapacity(heur.capacityFactor);
        dto.setFactorCondition(heur.conditionFactor);
        dto.setFactorMileage(heur.mileageFactor);
        dto.setFactorHealth(heur.healthFactor);
    }

    /**
     * Ghi Log sự kiện định giá có cấu trúc (JSON) sử dụng logger chuyên dụng.
     */
    private void logStructured(PricingSuggestResponseDTO dto, String cacheKey, int attempts, String modelUsed) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
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
            pricingLogger.info(json); // Sử dụng logger riêng
        } catch (Exception e) {
            logger.warn("Failed structured pricing log: {}", e.getMessage());
        }
    }
}