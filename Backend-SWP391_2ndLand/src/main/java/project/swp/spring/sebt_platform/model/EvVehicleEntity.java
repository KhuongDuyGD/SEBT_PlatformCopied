package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.model.enums.VehicleCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ev_vehicles",
    indexes = {
        @Index(name = "idx_ev_vehicles_brand", columnList = "brand"),
        @Index(name = "idx_ev_vehicles_type", columnList = "type"),
        @Index(name = "idx_ev_vehicles_year", columnList = "year")
    }
)
public class EvVehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private VehicleType type;

    @Column(name = "name", length = 200, nullable = false, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(name = "model", length = 200, columnDefinition = "NVARCHAR(200)")
    private String model;

    @Column(name = "brand", length = 200, nullable = false, columnDefinition = "NVARCHAR(200)")
    private String brand;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "mileage", columnDefinition = "INT DEFAULT 0")
    private Integer mileage = 0;

    @Column(name = "battery_capacity", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false, length = 30, columnDefinition = "NVARCHAR(30)")
    private VehicleCondition conditionStatus = VehicleCondition.GOOD;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    // Relationships
    @JsonIgnore // Add this annotation to prevent deep nesting
    @OneToMany(mappedBy = "evVehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductEntity> products;

    // Constructors
    public EvVehicleEntity() {}

    public EvVehicleEntity(VehicleType type, String name, String brand, Integer year) {
        this.type = type;
        this.name = name;
        this.brand = brand;
        this.year = year;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
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

    public VehicleCondition getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(VehicleCondition conditionStatus) {
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
