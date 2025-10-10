package project.swp.spring.sebt_platform.dto.request;

import java.util.Map;

/**
 * Request payload for pricing suggestion.
 * Fields kept flexible using maps for product/location to avoid large refactor.
 */
public class PricingSuggestRequestDTO {

    private String title;
    private String description;
    private String category; // EV or BATTERY
    private Map<String, Object> product; // brand, model, year, batteryCapacity, condition, healthPercentage, mileage
    private Map<String, Object> location; // province, district

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Map<String, Object> getProduct() { return product; }
    public void setProduct(Map<String, Object> product) { this.product = product; }

    public Map<String, Object> getLocation() { return location; }
    public void setLocation(Map<String, Object> location) { this.location = location; }
}
