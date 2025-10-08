package project.swp.spring.sebt_platform.dto.request;

import project.swp.spring.sebt_platform.model.enums.VehicleType;

import java.math.BigDecimal;

public record EvFilterFormDTO(
        VehicleType vehicleType,
        Integer year,
        String brand,
        String location,
        Integer minBatteryCapacity,
        Integer maxBatteryCapacity,
        Double minPrice,
        Double maxPrice
) {}
