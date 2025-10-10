package project.swp.spring.sebt_platform.pricing.baseline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaselinePriceEntry {
    private String brand;
    private String model;
    private String variant;
    private String category;
    private long baselinePrice;
    private String notes; // depreciation hints

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getBaselinePrice() { return baselinePrice; }
    public void setBaselinePrice(long baselinePrice) { this.baselinePrice = baselinePrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
