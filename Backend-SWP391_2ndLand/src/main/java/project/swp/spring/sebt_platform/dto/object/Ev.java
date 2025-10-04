package project.swp.spring.sebt_platform.dto.object;

import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.model.enums.VehicleType;

public class Ev {
    private VehicleType type;
    private String name;
    private String model;
    private String brand;
    private Integer year;
    private Integer mileage;
    private double batteryCapacity;
    private VehicleCondition conditionStatus;

    // Default constructor
    public Ev() {}

    // Constructor with parameters
    public Ev(VehicleType type, String name, String model, String brand,
              Integer year, Integer mileage, double batteryCapacity,
              VehicleCondition conditionStatus) {
        this.type = type;
        this.name = name;
        this.model = model;
        this.brand = brand;
        this.year = year;
        this.mileage = mileage;
        this.batteryCapacity = batteryCapacity;
        this.conditionStatus = conditionStatus;
    }

    // Getters and Setters
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }

    public double getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(double batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public VehicleCondition getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(VehicleCondition conditionStatus) { this.conditionStatus = conditionStatus; }
}
