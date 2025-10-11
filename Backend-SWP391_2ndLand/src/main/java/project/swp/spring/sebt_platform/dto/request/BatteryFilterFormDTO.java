package project.swp.spring.sebt_platform.dto.request;

/**
 * Battery filter DTO with nullable numeric fields for optional query parameters.
 */
public record BatteryFilterFormDTO(
        String brand,
        String location,
        String compatibility,
        Integer minBatteryCapacity,
        Integer maxBatteryCapacity,
        Double minPrice,
        Double maxPrice
) {}
