package project.swp.spring.sebt_platform.dto.object;

import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

public class Battery {
    private String brand;
    private String model;
    private double capacity;
    private int healthPercentage;
    private String compatibleVehicles;
    private BatteryCondition conditionStatus;

    // Default constructor
    public Battery() {}

    // Constructor with parameters
    public Battery(String brand, double capacity,
                   int healthPercentage, String compatibleVehicles,
                   BatteryCondition conditionStatus) {
        this.brand = brand;
        this.capacity = capacity;
        this.healthPercentage = healthPercentage;
        this.compatibleVehicles = compatibleVehicles;
        this.conditionStatus = conditionStatus;
    }

    // Getters and Setters
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }

    public int getHealthPercentage() { return healthPercentage; }
    public void setHealthPercentage(int healthPercentage) { this.healthPercentage = healthPercentage; }

    public String getCompatibleVehicles() { return compatibleVehicles; }
    public void setCompatibleVehicles(String compatibleVehicles) { this.compatibleVehicles = compatibleVehicles; }

    public BatteryCondition getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(BatteryCondition conditionStatus) { this.conditionStatus = conditionStatus; }
}
