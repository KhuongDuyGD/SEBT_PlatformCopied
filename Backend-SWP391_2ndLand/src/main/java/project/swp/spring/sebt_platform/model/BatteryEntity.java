package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
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

    @Column(length = 100, nullable = false)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(nullable = false)
    private Double capacity;

    @Column(nullable = false)
    private Integer healthPercentage;

    @Column(columnDefinition = "TEXT")
    private String compatibleVehicles;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatteryCondition conditionStatus = BatteryCondition.GOOD;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Normalize compatibleVehicles via join table if querying needed.
    // Getters and setters ...
}

