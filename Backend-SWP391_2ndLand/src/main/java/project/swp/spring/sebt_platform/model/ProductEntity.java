package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "products",
    indexes = {
        @Index(name = "idx_products_category_id", columnList = "category_id")
    }
)
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ev_id")
    private EvVehicleEntity evVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battery_id")
    private BatteryEntity battery;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Logical constraint (enforce at DB): exactly ONE of (ev_id, battery_id) must be NOT NULL.

    // Constructors
    public ProductEntity() {}

    public ProductEntity(CategoryEntity category) {
        this.category = category;
    }

    public ProductEntity(CategoryEntity category, EvVehicleEntity evVehicle) {
        this.category = category;
        this.evVehicle = evVehicle;
    }

    public ProductEntity(CategoryEntity category, BatteryEntity battery) {
        this.category = category;
        this.battery = battery;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
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
}
