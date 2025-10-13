package project.swp.spring.sebt_platform.dto.request;

import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.model.enums.VehicleType;

/**
 * DTO cho việc lọc xe điện với các tiêu chí đầy đủ
 * Sử dụng wrapper types để null có nghĩa là "không áp dụng filter"
 * Controller sẽ xây dựng từ query params; service layer phải xử lý null
 */
public record EvFilterFormDTO(
        VehicleType vehicleType,     // Loại xe: CAR, MOTORBIKE, BIKE
        Integer year,                // Năm sản xuất (exact match)
        Integer minYear,             // Năm sản xuất tối thiểu (từ năm X)
        Integer maxYear,             // Năm sản xuất tối đa (đến năm Y)
        String brand,                // Thương hiệu xe (exact match, case-insensitive)
        String province,             // Tỉnh/thành phố (location filter)
        String district,             // Quận/huyện (location filter) 
        VehicleCondition conditionStatus, // Tình trạng xe: EXCELLENT, GOOD, FAIR, POOR, NEEDS_MAINTENANCE
        Integer minMileage,          // Số km đã đi tối thiểu
        Integer maxMileage,          // Số km đã đi tối đa
        Integer minBatteryCapacity,  // Dung lượng pin tối thiểu (kWh)
        Integer maxBatteryCapacity,  // Dung lượng pin tối đa (kWh)
        Double minPrice,             // Giá tối thiểu (VND)
        Double maxPrice              // Giá tối đa (VND)
) {}
