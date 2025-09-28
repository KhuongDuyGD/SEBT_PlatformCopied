package project.swp.spring.sebt_platform.dto.object;

import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

public record Battery(
         String brand,
         String model,
         double capacity,
         int healthPercentage,
         String compatibleVehicles,
         BatteryCondition conditionStatus
){}
