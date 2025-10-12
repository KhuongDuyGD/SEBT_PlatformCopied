package project.swp.spring.sebt_platform.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ev_id")
    private EvVehicleEntity evVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battery_id")
    private BatteryEntity battery;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ProductEntity() {}

    public ProductEntity(EvVehicleEntity evVehicle) {
        this.evVehicle = evVehicle;
    }

    public ProductEntity(BatteryEntity battery) {
        this.battery = battery;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvVehicleEntity getEvVehicle() {
        return evVehicle;
    }

    public void setEvVehicle(EvVehicleEntity evVehicle) {
        this.evVehicle = evVehicle;
    }

    public BatteryEntity getBattery() {
        return battery;
    }

    public void setBattery(BatteryEntity battery) {
        this.battery = battery;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods to check product type
    public boolean isVehicleProduct() {
        return evVehicle != null && battery == null;
    }

    public boolean isBatteryProduct() {
        return battery != null && evVehicle == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity)) return false;
        ProductEntity that = (ProductEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
