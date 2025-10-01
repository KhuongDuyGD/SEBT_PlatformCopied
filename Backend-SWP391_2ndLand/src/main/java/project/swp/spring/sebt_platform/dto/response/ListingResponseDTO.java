package project.swp.spring.sebt_platform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.ListingType;

/**
 * DTO để trả về thông tin listing cho frontend
 * Bao gồm tất cả thông tin cần thiết để hiển thị một listing
 */
public record ListingResponseDTO(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String mainImage,
        List<String> images,
        ListingStatus status,
        ListingType listingType,
        Integer viewsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiresAt,
        
        // Thông tin người bán
        SellerInfoDTO seller,
        
        // Thông tin sản phẩm
        ProductInfoDTO product,
        
        // Thông tin vị trí
        LocationInfoDTO location
) {
    
    /**
     * DTO chứa thông tin cơ bản của người bán
     */
    public record SellerInfoDTO(
            Long id,
            String username,
            String avatar
    ) {}
    
    /**
     * DTO chứa thông tin sản phẩm (xe hoặc pin)
     */
    public record ProductInfoDTO(
            Long id,
            String productType, // "VEHICLE" hoặc "BATTERY"
            VehicleInfoDTO vehicle, // null nếu là battery
            BatteryInfoDTO battery   // null nếu là vehicle
    ) {}
    
    /**
     * DTO chứa thông tin xe điện
     */
    public record VehicleInfoDTO(
            Long id,
            String type,
            String name,
            String model,
            String brand,
            Integer year,
            Integer mileage,
            BigDecimal batteryCapacity,
            String conditionStatus
    ) {}
    
    /**
     * DTO chứa thông tin pin
     */
    public record BatteryInfoDTO(
            Long id,
            String brand,
            String model,
            BigDecimal capacity,
            Integer healthPercentage,
            String compatibleVehicles,
            String conditionStatus
    ) {}
    
    /**
     * DTO chứa thông tin vị trí
     */
    public record LocationInfoDTO(
            Long id,
            String province,
            String district,
            String details
    ) {}
}
