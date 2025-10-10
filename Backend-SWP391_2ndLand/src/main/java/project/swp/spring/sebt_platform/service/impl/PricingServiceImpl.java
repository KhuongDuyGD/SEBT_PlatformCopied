package project.swp.spring.sebt_platform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
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

        Long heuristic = heuristicSuggest(request);
        // Define clamp band around heuristic (if available)
    double pct = clampPercent <= 0 ? 0.15 : clampPercent; // safety default
    Long min = heuristic == null ? null : Math.round(heuristic * (1 - pct) / 1000.0) * 1000L;
    Long max = heuristic == null ? null : Math.round(heuristic * (1 + pct) / 1000.0) * 1000L;
        final String promptVersion = "v2";

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            logger.warn("Gemini API key not configured (app.ai.gemini.apiKey / GEMINI_API_KEY). Using heuristic fallback only.");
            PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, promptVersion, "Heuristic: brand/model/year/condition/battery (no API key)");
            resp.setPrompt(null);
            resp.setCacheHit(false);
            CACHE.put(cacheKey, resp);
            return resp;
        }
        String primaryModel = (geminiModel == null || geminiModel.isBlank()) ? "gemini-2.5-flash" : geminiModel.trim();
        String modelInUse = primaryModel;
        String prompt = buildPromptV2(request, heuristic, min, max, promptVersion);

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
                    result = postProcessGeminiResponse(response.getBody(), modelInUse, heuristic, min, max, promptVersion, prompt);
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
                    result = baseResponseFromHeuristic(heuristic, min, max, promptVersion, "Fallback heuristic after Gemini failure: HTTP " + response.getStatusCode().value());
                }
                result.setPrompt(truncate(prompt, 4000));
                result.setCacheHit(false);
                putCache(cacheKey, result);
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
                PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, promptVersion, userReason);
                resp.setPrompt(truncate(prompt, 4000));
                resp.setCacheHit(false);
                putCache(cacheKey, resp);
                return resp;
            } catch (Exception e) {
                logger.error("Gemini exception attempt={} type={} msg={}", attempts, e.getClass().getSimpleName(), e.getMessage());
                if (attempts < retryAttempts) {
                    sleepBackoff(attempts);
                    continue;
                }
                PricingSuggestResponseDTO resp = baseResponseFromHeuristic(heuristic, min, max, promptVersion, "Fallback heuristic after exception: " + e.getClass().getSimpleName());
                resp.setPrompt(truncate(prompt, 4000));
                resp.setCacheHit(false);
                putCache(cacheKey, resp);
                return resp;
            }
        }
    }

    private String truncate(String s, int max) { return s == null ? null : (s.length() <= max ? s : s.substring(0, max) + "..."); }

    private PricingSuggestResponseDTO postProcessGeminiResponse(String body, String model, Long heuristic, Long min, Long max, String promptVersion, String prompt) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                StringBuilder sb = new StringBuilder();
                candidates.forEach(c -> {
                    JsonNode parts = c.path("content").path("parts");
                    if (parts.isArray()) {
                        parts.forEach(p -> {
                            String t = p.path("text").asText();
                            if (!t.isBlank()) sb.append(t).append("\n");
                        });
                    }
                });
                String aggregated = sb.toString().trim();
                int start = aggregated.indexOf('{');
                int end = aggregated.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    String jsonSlice = aggregated.substring(start, end + 1);
                    try {
                        JsonNode obj = objectMapper.readTree(jsonSlice);
                        Double price = obj.path("suggestedPrice").isNumber() ? obj.path("suggestedPrice").asDouble() : null;
                        String reason = obj.path("reasoning").asText(null);
                        if (price != null && price > 0) {
                            return enrichedFinalFromAi(price, reason != null ? reason : "AI JSON parsed", model, heuristic, min, max, promptVersion, prompt);
                        }
                    } catch (Exception ignored) {
                        logger.warn("Failed to parse JSON slice from Gemini text");
                    }
                }
                Double extracted = extractFirstNumber(aggregated);
                if (extracted != null && extracted > 0) {
                    return enrichedFinalFromAi(extracted, "Parsed number from Gemini text", model, heuristic, min, max, promptVersion, prompt);
                }
                return enrichedFinalFromAi(null, "Gemini responded but no price parsed", model, heuristic, min, max, promptVersion, prompt);
            }
        } catch (Exception e) {
            logger.error("Parse Gemini response error: {}", e.getMessage());
        }
        return enrichedFinalFromAi(null, "Unable to parse Gemini response", model, heuristic, min, max, promptVersion, prompt);
    }

    private Double extractFirstNumber(String text) {
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d[\\d.,]{3,})").matcher(text.replace(",", ""));
            if (m.find()) {
                String num = m.group(1).replaceAll("[^0-9.]", "");
                return Double.parseDouble(num);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String buildPromptV2(PricingSuggestRequestDTO req, Long heuristic, Long min, Long max, String version) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROMPT_VERSION=" + version + "\n");
        sb.append("Bạn là chuyên gia thẩm định giá xe điện & pin tại Việt Nam.\n");
        sb.append("Trả về JSON duy nhất: {\n  \"suggestedPrice\": <số nguyên VND>,\n  \"reasoning\": \"<=2 câu tiếng Việt\"\n}\n");
        if (heuristic != null) {
            sb.append("Heuristic: ").append(heuristic).append(" VND\n");
            sb.append("PHẠM VI CHO PHÉP: min=").append(min).append(", max=").append(max).append("\n");
            sb.append("Nếu bạn muốn vượt ngoài phạm vi => giữ trong phạm vi và giải thích ngắn gọn.\n");
        }
        sb.append("Không thêm đơn vị hay dấu chấm phẩy trong số. Không format.\n");
        sb.append("DỮ LIỆU:\n");
        sb.append("category=").append(req.getCategory()).append("\n");
        if (req.getProduct() != null) req.getProduct().forEach((k,v) -> sb.append("product.").append(k).append("=").append(v).append("\n"));
        if (req.getLocation() != null) req.getLocation().forEach((k,v) -> sb.append("location.").append(k).append("=").append(v).append("\n"));
        sb.append("title=").append(req.getTitle()).append("\n");
        sb.append("description=").append(req.getDescription()).append("\n");
        return sb.toString();
    }

    private Long heuristicSuggest(PricingSuggestRequestDTO req) {
        try {
            Map<String,Object> p = req.getProduct();
            if (p == null) return null;
            String brand = optString(p.get("brand"));
            String model = optString(p.get("model"));
            Integer year = optInt(p.get("year"));
            Double batteryCapacity = optDouble(p.get("batteryCapacity"));
            String condition = optString(p.getOrDefault("condition", "Used"));
            Integer mileage = optInt(p.get("mileage"));
            Double health = optDouble(p.get("healthPercentage"));

            double base = 300_000_000; // default base
            if (brand != null) {
                if (brand.equalsIgnoreCase("VinFast")) base = 350_000_000;
                else if (brand.equalsIgnoreCase("Yadea")) base = 25_000_000;
                else if (brand.equalsIgnoreCase("Dat Bike")) base = 49_000_000;
            }
            if (model != null) {
                if (model.toLowerCase().contains("feliz")) base = 27_000_000;
                if (model.toLowerCase().contains("klara")) base = 35_000_000;
            }
            int currentYear = java.time.Year.now().getValue();
            if (year != null && year >= 2018 && year <= currentYear) {
                int age = currentYear - year;
                base *= Math.pow(0.92, age);
            }
            if (batteryCapacity != null) base *= (1 + Math.min(0.5, batteryCapacity / 150.0));
            if (condition != null) {
                switch (condition.toLowerCase()) {
                    case "new": base *= 1.05; break;
                    case "used_good": case "good": base *= 0.95; break;
                    case "used_fair": case "fair": base *= 0.85; break;
                    case "used_poor": case "poor": base *= 0.70; break;
                }
            }
            if (mileage != null) base *= (1 - Math.min(0.4, mileage / 100_000.0));
            if (health != null && health > 0) base *= (0.5 + Math.min(0.6, health / 100.0));
            base = Math.max(1_000_000, Math.min(base, 2_000_000_000));
            return Math.round(base / 1000.0) * 1000L;
        } catch (Exception e) {
            return null;
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
            return dto;
        }
        PricingSuggestResponseDTO dto = new PricingSuggestResponseDTO(heuristic, reason, null, "heuristic", heuristic, min, max, false, 1.0, 0.0, promptVersion);
        return dto;
    }

    private PricingSuggestResponseDTO enrichedFinalFromAi(Double aiPriceRaw, String reason, String model, Long heuristic, Long min, Long max, String promptVersion, String prompt) {
        Long aiRounded = aiPriceRaw == null ? null : roundToThousand(aiPriceRaw);
        boolean clamped = false;
        Long finalPrice = aiRounded;
        if (finalPrice == null && heuristic != null) finalPrice = heuristic;
        if (finalPrice != null && min != null && max != null) {
            if (finalPrice < min) { finalPrice = min; clamped = true; }
            else if (finalPrice > max) { finalPrice = max; clamped = true; }
        }
        Double deltaPercent = null, confidence = null;
        if (heuristic != null && finalPrice != null) {
            deltaPercent = (finalPrice - heuristic) * 100.0 / heuristic;
            confidence = 1.0 - Math.min(1.0, Math.abs(finalPrice - heuristic) / (double) heuristic);
        }
    PricingSuggestResponseDTO dto = new PricingSuggestResponseDTO(finalPrice, reason, model, aiRounded == null ? "heuristic" : "gemini",
                heuristic, min, max, clamped, confidence, deltaPercent, promptVersion);
        // Tạm reuse 'reason' field cho prompt debug nếu cần – có thể mở rộng sang field mới nếu muốn.
        return dto;
    }
}
