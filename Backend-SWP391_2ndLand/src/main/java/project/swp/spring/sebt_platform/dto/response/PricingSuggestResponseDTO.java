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

    // --- Breakdown fields (mới thêm) ---
    private Long baselinePrice;         // Baseline theo model/brand trước khi nhân hệ số
    private Double clampPercent;        // biên clamp động (ví dụ 0.12 nghĩa ±12%)
    // Các hệ số thành phần
    private Double factorAge;
    private Double factorCapacity;
    private Double factorCondition;
    private Double factorMileage;
    private Double factorHealth;

    // Depreciation strategy metadata
    private String strategyType;      // LINEAR | EXPONENTIAL | NONE
    private Double strategyRate;      // %/year (linear) hoặc factor (exponential)
    private Double strategyMaxDep;    // Tổng khấu hao tối đa (fraction), có thể null nếu không set

    // Evidence tags: nguồn / yếu tố tạo nên quyết định giá (baseline, depreciation, capacity, mileage, condition, health, market, adjustment, clamp)
    @io.swagger.v3.oas.annotations.media.Schema(description = "Các nhãn giải thích: baseline,depreciation,capacity,mileage,condition,health,market,adjustment,clamp,heuristic")
    private java.util.List<String> evidence; // optional; AI sẽ cung cấp + backend có thể bổ sung

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
    public Long getBaselinePrice() { return baselinePrice; }
    public void setBaselinePrice(Long baselinePrice) { this.baselinePrice = baselinePrice; }
    public Double getClampPercent() { return clampPercent; }
    public void setClampPercent(Double clampPercent) { this.clampPercent = clampPercent; }
    public Double getFactorAge() { return factorAge; }
    public void setFactorAge(Double factorAge) { this.factorAge = factorAge; }
    public Double getFactorCapacity() { return factorCapacity; }
    public void setFactorCapacity(Double factorCapacity) { this.factorCapacity = factorCapacity; }
    public Double getFactorCondition() { return factorCondition; }
    public void setFactorCondition(Double factorCondition) { this.factorCondition = factorCondition; }
    public Double getFactorMileage() { return factorMileage; }
    public void setFactorMileage(Double factorMileage) { this.factorMileage = factorMileage; }
    public Double getFactorHealth() { return factorHealth; }
    public void setFactorHealth(Double factorHealth) { this.factorHealth = factorHealth; }
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public Double getStrategyRate() { return strategyRate; }
    public void setStrategyRate(Double strategyRate) { this.strategyRate = strategyRate; }
    public Double getStrategyMaxDep() { return strategyMaxDep; }
    public void setStrategyMaxDep(Double strategyMaxDep) { this.strategyMaxDep = strategyMaxDep; }
    public java.util.List<String> getEvidence() { return evidence; }
    public void setEvidence(java.util.List<String> evidence) { this.evidence = evidence; }
}
