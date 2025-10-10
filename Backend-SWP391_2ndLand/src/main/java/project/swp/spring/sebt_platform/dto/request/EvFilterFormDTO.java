package project.swp.spring.sebt_platform.dto.request;

import project.swp.spring.sebt_platform.model.enums.VehicleType;

/**
 * EV filter DTO now uses wrapper types so that null means "not supplied".
 * Controller will build this from query params; service layer must handle null.
 */
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
