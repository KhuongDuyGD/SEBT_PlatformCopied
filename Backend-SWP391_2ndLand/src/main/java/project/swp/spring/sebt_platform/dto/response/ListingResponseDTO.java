package project.swp.spring.sebt_platform.model.dto.response;

import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.ListingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ListingResponseDTO {
    private Long id;
    private String title;
    private String location;
    private String description;
    private BigDecimal price;
    private ListingStatus status;
    private ListingType listingType;
    private String mainImage;
    private List<String> imageUrls;
    private Integer viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Product details
    private String productType; // "VEHICLE" or "BATTERY"
    private VehicleDetailsDTO vehicleDetails;
    private BatteryDetailsDTO batteryDetails;

    // Nested classes for product details
    public static class VehicleDetailsDTO {
        private String type;
        private String name;
        private String model;
        private String brand;
        private Integer year;
        private Integer mileage;
        private Double batteryCapacity;
        private String conditionStatus;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Integer getMileage() { return mileage; }
        public void setMileage(Integer mileage) { this.mileage = mileage; }

        public Double getBatteryCapacity() { return batteryCapacity; }
        public void setBatteryCapacity(Double batteryCapacity) { this.batteryCapacity = batteryCapacity; }

        public String getConditionStatus() { return conditionStatus; }
        public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
    }

    public static class BatteryDetailsDTO {
        private String brand;
        private String model;
        private Double capacity;
        private Integer healthPercentage;
        private String compatibleVehicles;
        private String batteryDescription;
        private String conditionStatus;

        // Getters and Setters
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public Double getCapacity() { return capacity; }
        public void setCapacity(Double capacity) { this.capacity = capacity; }

        public Integer getHealthPercentage() { return healthPercentage; }
        public void setHealthPercentage(Integer healthPercentage) { this.healthPercentage = healthPercentage; }

        public String getCompatibleVehicles() { return compatibleVehicles; }
        public void setCompatibleVehicles(String compatibleVehicles) { this.compatibleVehicles = compatibleVehicles; }

        public String getBatteryDescription() { return batteryDescription; }
        public void setBatteryDescription(String batteryDescription) { this.batteryDescription = batteryDescription; }

        public String getConditionStatus() { return conditionStatus; }
        public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
    }

    // Main class Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public ListingType getListingType() { return listingType; }
    public void setListingType(ListingType listingType) { this.listingType = listingType; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public VehicleDetailsDTO getVehicleDetails() { return vehicleDetails; }
    public void setVehicleDetails(VehicleDetailsDTO vehicleDetails) { this.vehicleDetails = vehicleDetails; }

    public BatteryDetailsDTO getBatteryDetails() { return batteryDetails; }
    public void setBatteryDetails(BatteryDetailsDTO batteryDetails) { this.batteryDetails = batteryDetails; }
}
