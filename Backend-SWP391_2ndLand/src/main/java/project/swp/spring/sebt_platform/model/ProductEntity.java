package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ev_id")
    private EvVehicleEntity evVehicle;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "battery_id")
    private BatteryEntity battery;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ListingEntity listing;

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

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    // Helper methods to check product type
    public boolean isVehicleProduct() {
        return evVehicle != null && battery == null;
    }

    public boolean isBatteryProduct() {
        return battery != null && evVehicle == null;
    }
}
