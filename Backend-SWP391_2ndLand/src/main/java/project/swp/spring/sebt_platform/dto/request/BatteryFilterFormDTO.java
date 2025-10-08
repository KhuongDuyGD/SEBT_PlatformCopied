package project.swp.spring.sebt_platform.dto.request;


import java.time.LocalDateTime;

public record BatteryFilterFormDTO(
        String brand,
        String location,
        String compatibility,
        Integer minBatteryCapacity,
        Integer maxBatteryCapacity,
        Double minPrice,
        Double maxPrice
) {}
