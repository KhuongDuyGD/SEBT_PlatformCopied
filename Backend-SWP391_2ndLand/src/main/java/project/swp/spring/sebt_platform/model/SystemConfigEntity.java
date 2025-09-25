package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.ConfigDataType;

@Entity
@Table(name = "system_configs",
    indexes = {
        @Index(name = "idx_system_configs_config_key", columnList = "config_key"),
        @Index(name = "idx_system_configs_is_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_system_configs_config_key", columnNames = "config_key")
    }
)
public class SystemConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String configKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConfigDataType dataType = ConfigDataType.STRING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Note: Consider namespacing keys (e.g., payment.timeout). Add encrypted flag for secrets.

    // Constructors
    public SystemConfigEntity() {}

    public SystemConfigEntity(String configKey, String configValue, ConfigDataType dataType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.dataType = dataType;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public ConfigDataType getDataType() {
        return dataType;
    }

    public void setDataType(ConfigDataType dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
