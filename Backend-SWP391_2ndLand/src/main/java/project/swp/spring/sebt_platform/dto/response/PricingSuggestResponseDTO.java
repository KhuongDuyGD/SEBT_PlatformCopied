package project.swp.spring.sebt_platform.dto.response;

/**
 * Response payload for pricing suggestion.
 */
public class PricingSuggestResponseDTO {
    private Long suggestedPrice;        // Giá cuối cùng trả về (đã clamp nếu cần)
    private String reason;              // Giải thích / justification / fallback message
    private String model;               // Tên model AI dùng
    private String mode;                // heuristic | gemini

    // --- Enriched fields ---
    private Long heuristicPrice;        // Giá heuristic gốc
    private Long minPrice;              // Biên dưới clamp
    private Long maxPrice;              // Biên trên clamp
    private Boolean clamped;            // Có bị ép vào range không
    private Double confidence;          // 0..1 dựa trên khoảng cách so với heuristic
    private Double deltaPercent;        // % chênh lệch so với heuristic (dấu + / -)
    private String promptVersion;       // version prompt (để front hiển thị / debug)
    private String prompt;              // prompt đã gửi lên AI (truncated)
    private Boolean cacheHit;           // true nếu lấy từ cache server

    public PricingSuggestResponseDTO() {}

    public PricingSuggestResponseDTO(Long suggestedPrice, String reason, String model, String mode) {
        this.suggestedPrice = suggestedPrice;
        this.reason = reason;
        this.model = model;
        this.mode = mode;
    }

    // Builder-style convenience constructor for enriched response
    public PricingSuggestResponseDTO(Long suggestedPrice, String reason, String model, String mode,
                                     Long heuristicPrice, Long minPrice, Long maxPrice,
                                     Boolean clamped, Double confidence, Double deltaPercent,
                                     String promptVersion) {
        this.suggestedPrice = suggestedPrice;
        this.reason = reason;
        this.model = model;
        this.mode = mode;
        this.heuristicPrice = heuristicPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.clamped = clamped;
        this.confidence = confidence;
        this.deltaPercent = deltaPercent;
        this.promptVersion = promptVersion;
    }

    public Long getSuggestedPrice() { return suggestedPrice; }
    public void setSuggestedPrice(Long suggestedPrice) { this.suggestedPrice = suggestedPrice; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Long getHeuristicPrice() { return heuristicPrice; }
    public void setHeuristicPrice(Long heuristicPrice) { this.heuristicPrice = heuristicPrice; }
    public Long getMinPrice() { return minPrice; }
    public void setMinPrice(Long minPrice) { this.minPrice = minPrice; }
    public Long getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Long maxPrice) { this.maxPrice = maxPrice; }
    public Boolean getClamped() { return clamped; }
    public void setClamped(Boolean clamped) { this.clamped = clamped; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public Double getDeltaPercent() { return deltaPercent; }
    public void setDeltaPercent(Double deltaPercent) { this.deltaPercent = deltaPercent; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public Boolean getCacheHit() { return cacheHit; }
    public void setCacheHit(Boolean cacheHit) { this.cacheHit = cacheHit; }
}
