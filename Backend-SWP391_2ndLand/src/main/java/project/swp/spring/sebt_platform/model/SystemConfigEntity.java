package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import project.swp.spring.sebt_platform.model.enums.ConfigDataType;

import java.time.LocalDateTime;

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

    @Column(name = "config_key", length = 100, nullable = false, unique = true, columnDefinition = "VARCHAR(100)")
    private String configKey;

    @Column(name = "config_value", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private ConfigDataType dataType = ConfigDataType.STRING;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    // Constructors
    public SystemConfigEntity() {}

    public SystemConfigEntity(String configKey, String configValue, ConfigDataType dataType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.dataType = dataType;
    }

    public SystemConfigEntity(String configKey, String configValue, ConfigDataType dataType, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.dataType = dataType;
        this.description = description;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
