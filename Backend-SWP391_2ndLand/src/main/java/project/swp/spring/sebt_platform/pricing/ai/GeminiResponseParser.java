package project.swp.spring.sebt_platform.pricing.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Isolated parser for Gemini responses.
 * Responsibilities:
 *  - Aggregate candidate text
 *  - Extract first JSON object slice
 *  - Parse suggestedPrice, reasoning, evidence[]
 *  - Filter/normalize evidence tags
 *  - Provide fallback to numeric extraction
 */
public class GeminiResponseParser {
    private static final Logger log = LoggerFactory.getLogger(GeminiResponseParser.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public record ParseResult(Double price, String reasoning, List<String> evidence, String rawAggregated, boolean jsonParsed, boolean numberFallback) {}

    private static final Set<String> ALLOWED_EVIDENCE = Set.of(
            "baseline","depreciation","capacity","mileage","condition","health","market","adjustment","clamp","heuristic"
    );

    public ParseResult parse(String body) {
        if (body == null || body.isBlank()) return new ParseResult(null, null, List.of(), "", false, false);
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                StringBuilder agg = new StringBuilder();
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
                // Try JSON slice
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
                // Fallback: extract first number
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

    private List<String> filterEvidence(List<String> ev) {
        if (ev == null) return List.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String e : ev) {
            if (e == null) continue;
            String norm = e.trim().toLowerCase();
            if (norm.equals("age")) norm = "depreciation"; // unify
            if (ALLOWED_EVIDENCE.contains(norm)) out.add(norm);
        }
        return new ArrayList<>(out);
    }

    private Double extractFirstNumber(String text) {
        try {
            // Allow spaces inside large numbers (e.g., "8 900 000") by permitting whitespace in the sequence
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d[\\d\\s.,]{3,})")
                    .matcher(text);
            if (m.find()) {
                String num = m.group(1).replaceAll("[^0-9]", "");
                return Double.parseDouble(num);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
