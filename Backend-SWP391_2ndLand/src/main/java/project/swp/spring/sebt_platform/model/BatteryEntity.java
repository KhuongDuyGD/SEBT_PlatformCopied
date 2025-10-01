package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "brand", length = 200, nullable = false, columnDefinition = "NVARCHAR(200)")
    private String brand;

    @Column(name = "model", length = 200, columnDefinition = "NVARCHAR(200)")
    private String model;

    @Column(name = "capacity", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal capacity;

    @Column(name = "health_percentage", nullable = false)
    private Integer healthPercentage;

    @Column(name = "compatible_vehicles", columnDefinition = "NVARCHAR(MAX)")
    private String compatibleVehicles;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false, length = 30, columnDefinition = "NVARCHAR(30)")
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

    public BatteryEntity(String brand, BigDecimal capacity, Integer healthPercentage) {
        this.brand = brand;
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
