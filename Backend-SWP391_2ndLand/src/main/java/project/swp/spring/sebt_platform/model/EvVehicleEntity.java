package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.model.enums.VehicleCondition;

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
    @Column(nullable = false, length = 20)
    private VehicleType type;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100)
    private String model;

    @Column(length = 100, nullable = false)
    private String brand;

    @Column(nullable = false)
    private Integer year;

    @Column
    private Integer mileage = 0;

    @Column
    private Double batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleCondition conditionStatus = VehicleCondition.GOOD;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Add CHECK year BETWEEN 1990 AND current year at DB level.

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

    public Double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Double batteryCapacity) {
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
}
