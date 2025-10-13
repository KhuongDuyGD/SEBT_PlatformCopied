package project.swp.spring.sebt_platform.dto.request;

import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

/**
 * DTO cho việc lọc pin điện với các tiêu chí đầy đủ
 * Sử dụng wrapper types để null có nghĩa là "không áp dụng filter"
 */
public record BatteryFilterFormDTO(
        String brand,                      // Thương hiệu pin (exact match, case-insensitive)
        String name,                       // Tên pin (partial match for autocomplete)
        Integer year,                      // Năm sản xuất (exact match)
        Integer minYear,                   // Năm sản xuất tối thiểu (từ năm X)
        Integer maxYear,                   // Năm sản xuất tối đa (đến năm Y)
        String province,                   // Tỉnh/thành phố (location filter)
        String district,                   // Quận/huyện (location filter)
        BatteryCondition conditionStatus,  // Tình trạng pin: EXCELLENT, GOOD, FAIR, POOR, NEEDS_REPLACEMENT
        String compatibility,              // Xe tương thích (partial match trong compatibleVehicles)
        Integer minBatteryCapacity,        // Dung lượng pin tối thiểu (kWh)
        Integer maxBatteryCapacity,        // Dung lượng pin tối đa (kWh)
        Integer minHealthPercentage,       // Sức khỏe pin tối thiểu (%)
        Integer maxHealthPercentage,       // Sức khỏe pin tối đa (%)
        Double minPrice,                   // Giá tối thiểu (VND)
        Double maxPrice                    // Giá tối đa (VND)
) {}
