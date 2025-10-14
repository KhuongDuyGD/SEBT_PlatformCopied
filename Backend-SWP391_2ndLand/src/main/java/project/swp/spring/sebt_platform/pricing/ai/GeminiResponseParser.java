package project.swp.spring.sebt_platform.pricing.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Lớp phân tích phản hồi (JSON/Text) từ API Gemini.
 * Chịu trách nhiệm trích xuất giá đề xuất, lý do, và các thẻ bằng chứng (evidence tags).
 */
public class GeminiResponseParser {
    private static final Logger log = LoggerFactory.getLogger(GeminiResponseParser.class);
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Record đại diện cho kết quả phân tích phản hồi của AI.
     */
    public record ParseResult(Double price, String reasoning, List<String> evidence, String rawAggregated, boolean jsonParsed, boolean numberFallback) {}

    private static final Set<String> ALLOWED_EVIDENCE = Set.of(
            "baseline","depreciation","capacity","mileage","condition","health","market","adjustment","clamp","heuristic"
    );

    /**
     * Phân tích chuỗi phản hồi thô từ Gemini.
     * Ưu tiên tìm kiếm và phân tích JSON hợp lệ. Nếu thất bại, sẽ thử Fallback sang trích xuất số.
     *
     * @param body Chuỗi phản hồi JSON thô.
     * @return {@link ParseResult} chứa giá và các thông tin liên quan.
     */
    public ParseResult parse(String body) {
        if (body == null || body.isBlank()) return new ParseResult(null, null, List.of(), "", false, false);
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                StringBuilder agg = new StringBuilder();
                // 1. Tổng hợp tất cả các phần văn bản từ phản hồi AI
                candidates.forEach(c -> {
                    JsonNode parts = c.path("content").path("parts");
                    if (parts.isArray()) {
                        parts.forEach(p -> {
                            String t = p.path("text").asText();
                            if (!t.isBlank()) agg.append(t).append('\n');
                        });
                    }
                });
                String aggregated = agg.toString().trim();

                // 2. Cố gắng trích xuất và phân tích JSON Slice (phần nội dung JSON)
                int start = aggregated.indexOf('{');
                int end = aggregated.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    String slice = aggregated.substring(start, end + 1);
                    try {
                        JsonNode obj = mapper.readTree(slice);
                        Double price = obj.path("suggestedPrice").isNumber() ? obj.path("suggestedPrice").asDouble() : null;
                        String reasoning = obj.path("reasoning").asText(null);
                        List<String> evidenceRaw = new ArrayList<>();
                        if (obj.path("evidence").isArray()) {
                            obj.path("evidence").forEach(ev -> { if (ev.isTextual()) evidenceRaw.add(ev.asText()); });
                        }
                        List<String> filteredEvidence = filterEvidence(evidenceRaw);
                        if (price != null && price > 0) {
                            return new ParseResult(price, reasoning, filteredEvidence, aggregated, true, false);
                        }
                    } catch (Exception ex) {
                        log.debug("JSON slice parse failed: {}", ex.getMessage());
                    }
                }

                // 3. Fallback: Trích xuất số đầu tiên nếu JSON parse thất bại
                Double extracted = extractFirstNumber(aggregated);
                if (extracted != null && extracted > 0) {
                    return new ParseResult(extracted, "Parsed number fallback", List.of(), aggregated, false, true);
                }

                return new ParseResult(null, "No price parsed", List.of(), aggregated, false, false);
            }
        } catch (Exception e) {
            log.debug("Body parse root error: {}", e.getMessage());
        }
        return new ParseResult(null, "Invalid body", List.of(), body, false, false);
    }

    /**
     * Lọc và chuẩn hóa các thẻ bằng chứng (evidence tags) từ AI.
     * Chỉ chấp nhận các thẻ nằm trong danh sách {@link #ALLOWED_EVIDENCE}.
     */
    private List<String> filterEvidence(List<String> ev) {
        if (ev == null) return List.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String e : ev) {
            if (e == null) continue;
            String norm = e.trim().toLowerCase();
            if (norm.equals("age")) norm = "depreciation"; // Chuẩn hóa 'age' thành 'depreciation'
            if (ALLOWED_EVIDENCE.contains(norm)) out.add(norm);
        }
        return new ArrayList<>(out);
    }

    /**
     * Trích xuất số có vẻ là giá (có ít nhất 4 chữ số liên tiếp) đầu tiên từ văn bản.
     */
    private Double extractFirstNumber(String text) {
        try {
            // Regex tìm kiếm chuỗi số có ít nhất 4 chữ số, cho phép khoảng trắng/dấu chấm/phẩy bên trong
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d[\\d\\s.,]{3,})")
                    .matcher(text);
            if (m.find()) {
                // Xóa tất cả ký tự không phải số để có chuỗi số nguyên
                String num = m.group(1).replaceAll("[^0-9]", "");
                return Double.parseDouble(num);
            }
        } catch (Exception ignored) {}
        return null;
    }
}