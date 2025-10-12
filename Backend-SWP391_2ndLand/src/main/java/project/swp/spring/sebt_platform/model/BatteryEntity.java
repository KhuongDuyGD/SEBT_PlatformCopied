package project.swp.spring.sebt_platform.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

@Entity
@Table(name = "batteries",
    indexes = {
        @Index(name = "idx_batteries_brand", columnList = "brand"),
        @Index(name = "idx_batteries_capacity", columnList = "capacity"),
        @Index(name = "idx_batteries_health_percentage", columnList = "health_percentage")
    }
)
public class BatteryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "brand", length = 200, nullable = false, columnDefinition = "NVARCHAR(50)")
    private String brand;

    @Column(name = "model", length = 200, columnDefinition = "NVARCHAR(50)")
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "capacity", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal capacity;

    @Column(name = "health_percentage", nullable = false)
    private Integer healthPercentage;

    @Column(name = "compatible_vehicles", columnDefinition = "NVARCHAR(MAX)")
    private String compatibleVehicles;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false, length = 30, columnDefinition = "NVARCHAR(20)")
    private BatteryCondition conditionStatus = BatteryCondition.GOOD;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    // Relationships
    @JsonIgnore // Add this annotation to prevent deep nesting
    @OneToMany(mappedBy = "battery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductEntity> products;

    // Constructors
    public BatteryEntity() {}

    public BatteryEntity(String name, String brand, String model, Integer year, BigDecimal capacity, Integer healthPercentage) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.capacity = capacity;
        this.healthPercentage = healthPercentage;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public BatteryCondition getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(BatteryCondition conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProductEntity> getProducts() {
        return products;
    }

    public void setProducts(List<ProductEntity> products) {
        this.products = products;
    }
}
