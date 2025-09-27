package project.swp.spring.sebt_platform.dto.object;

import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

public record Baterry(
         String brand,
         String model,
         double capacity,
         double healthPercentage,
         String compatibleVehicles,
         BatteryCondition conditionStatus
){}
