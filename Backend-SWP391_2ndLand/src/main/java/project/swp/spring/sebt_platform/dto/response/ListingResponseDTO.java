package project.swp.spring.sebt_platform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.ListingType;

public class ListingResponseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String mainImage;
    private List<String> images;
    private ListingStatus status;
    private ListingType listingType;
    private Integer viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private SellerInfoDTO seller;
    private ProductInfoDTO product;
    private LocationInfoDTO location;

    public ListingResponseDTO() {
    }

    public ListingResponseDTO(Long id, String title, String description, BigDecimal price,
                            String mainImage, List<String> images, ListingStatus status,
                            ListingType listingType, Integer viewsCount, LocalDateTime createdAt,
                            LocalDateTime updatedAt, LocalDateTime expiresAt, SellerInfoDTO seller,
                            ProductInfoDTO product, LocationInfoDTO location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.mainImage = mainImage;
        this.images = images;
        this.status = status;
        this.listingType = listingType;
        this.viewsCount = viewsCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.seller = seller;
        this.product = product;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public SellerInfoDTO getSeller() {
        return seller;
    }

    public void setSeller(SellerInfoDTO seller) {
        this.seller = seller;
    }

    public ProductInfoDTO getProduct() {
        return product;
    }

    public void setProduct(ProductInfoDTO product) {
        this.product = product;
    }

    public LocationInfoDTO getLocation() {
        return location;
    }

    public void setLocation(LocationInfoDTO location) {
        this.location = location;
    }

    // Nested classes
    public static class SellerInfoDTO {
        private Long id;
        private String username;
        private String avatar;

        public SellerInfoDTO() {
        }

        public SellerInfoDTO(Long id, String username, String avatar) {
            this.id = id;
            this.username = username;
            this.avatar = avatar;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }

    public static class ProductInfoDTO {
        private Long id;
        private String productType;
        private VehicleInfoDTO vehicle;
        private BatteryInfoDTO battery;

        public ProductInfoDTO() {
        }

        public ProductInfoDTO(Long id, String productType, VehicleInfoDTO vehicle, BatteryInfoDTO battery) {
            this.id = id;
            this.productType = productType;
            this.vehicle = vehicle;
            this.battery = battery;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }

        public VehicleInfoDTO getVehicle() {
            return vehicle;
        }

        public void setVehicle(VehicleInfoDTO vehicle) {
            this.vehicle = vehicle;
        }

        public BatteryInfoDTO getBattery() {
            return battery;
        }

        public void setBattery(BatteryInfoDTO battery) {
            this.battery = battery;
        }
    }

    public static class VehicleInfoDTO {
        private Long id;
        private String type;
        private String name;
        private String model;
        private String brand;
        private Integer year;
        private Integer mileage;
        private BigDecimal batteryCapacity;
        private String conditionStatus;

        public VehicleInfoDTO() {
        }

        public VehicleInfoDTO(Long id, String type, String name, String model, String brand,
                            Integer year, Integer mileage, BigDecimal batteryCapacity,
                            String conditionStatus) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.model = model;
            this.brand = brand;
            this.year = year;
            this.mileage = mileage;
            this.batteryCapacity = batteryCapacity;
            this.conditionStatus = conditionStatus;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getMileage() {
            return mileage;
        }

        public void setMileage(Integer mileage) {
            this.mileage = mileage;
        }

        public BigDecimal getBatteryCapacity() {
            return batteryCapacity;
        }

        public void setBatteryCapacity(BigDecimal batteryCapacity) {
            this.batteryCapacity = batteryCapacity;
        }

        public String getConditionStatus() {
            return conditionStatus;
        }

        public void setConditionStatus(String conditionStatus) {
            this.conditionStatus = conditionStatus;
        }
    }

    public static class BatteryInfoDTO {
        private Long id;
        private String brand;
        private String model;
        private BigDecimal capacity;
        private Integer healthPercentage;
        private String compatibleVehicles;
        private String conditionStatus;

        public BatteryInfoDTO() {
        }

        public BatteryInfoDTO(Long id, String brand, String model, BigDecimal capacity,
                            Integer healthPercentage, String compatibleVehicles,
                            String conditionStatus) {
            this.id = id;
            this.brand = brand;
            this.model = model;
            this.capacity = capacity;
            this.healthPercentage = healthPercentage;
            this.compatibleVehicles = compatibleVehicles;
            this.conditionStatus = conditionStatus;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public BigDecimal getCapacity() {
            return capacity;
        }

        public void setCapacity(BigDecimal capacity) {
            this.capacity = capacity;
        }

        public Integer getHealthPercentage() {
            return healthPercentage;
        }

        public void setHealthPercentage(Integer healthPercentage) {
            this.healthPercentage = healthPercentage;
        }

        public String getCompatibleVehicles() {
            return compatibleVehicles;
        }

        public void setCompatibleVehicles(String compatibleVehicles) {
            this.compatibleVehicles = compatibleVehicles;
        }

        public String getConditionStatus() {
            return conditionStatus;
        }

        public void setConditionStatus(String conditionStatus) {
            this.conditionStatus = conditionStatus;
        }
    }

    public static class LocationInfoDTO {
        private Long id;
        private String province;
        private String district;
        private String details;

        public LocationInfoDTO() {
        }

        public LocationInfoDTO(Long id, String province, String district, String details) {
            this.id = id;
            this.province = province;
            this.district = district;
            this.details = details;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}
