package project.swp.spring.sebt_platform.dto.object;

import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.model.enums.VehicleType;

public record Ev(
         VehicleType type,
         String name,
         String model,
         String brand,
         Integer year,
         Integer mileage,
         double batteryCapacity,
         VehicleCondition conditionStatus
){}
