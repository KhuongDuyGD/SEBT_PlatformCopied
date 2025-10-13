package project.swp.spring.sebt_platform.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.web.bind.annotation.RequestParam;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.enums.BatteryCondition;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.model.enums.VehicleType;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, Long>, JpaSpecificationExecutor<ListingEntity> {

    ListingEntity findById(long id);

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.seller.id =: userId " +
            "AND l.status =: listingStatus " +
            "ORDER BY l.createdAt ")
    List<ListingEntity> findByListingUserID(@Param("userId") Long userId, @Param("listingStatus") ListingStatus listingStatus);

    List<ListingEntity> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    Page<ListingEntity> findByTitleContainingIgnoreCaseAndStatus(String keyword, ListingStatus status, Pageable pageable);

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = :status " +
            "AND l.product.evVehicle IS NOT NULL " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> findEvListingsByStatus(@Param("status") ListingStatus status, Pageable pageable);

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = :status " +
            "AND l.product.battery IS NOT NULL " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> findBatteryListingsByStatus(@Param("status") ListingStatus status, Pageable pageable);

    Page<ListingEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<ListingEntity> findByTitleContaining(@Param("title") String title);

    @Query(
            "SELECT l FROM ListingEntity l " +
                    "JOIN LocationEntity lt ON l.id = lt.listing.id " +
                    "WHERE l.status = 'ACTIVE' " +
                    "AND (:year IS NULL OR l.product.evVehicle.year = :year) " +
                    "AND (:minYear IS NULL OR l.product.evVehicle.year >= :minYear) " +
                    "AND (:maxYear IS NULL OR l.product.evVehicle.year <= :maxYear) " +
                    "AND (:type IS NULL OR l.product.evVehicle.type = :type) " +
                    "AND (:brand IS NULL OR LOWER(l.product.evVehicle.brand) = LOWER(:brand)) " +
                    "AND (:province IS NULL OR LOWER(lt.province) LIKE LOWER(CONCAT('%', :province, '%'))) " +
                    "AND (:district IS NULL OR LOWER(lt.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
                    "AND (:conditionStatus IS NULL OR l.product.evVehicle.conditionStatus = :conditionStatus) " +
                    "AND (:minMileage IS NULL OR l.product.evVehicle.mileage >= :minMileage) " +
                    "AND (:maxMileage IS NULL OR l.product.evVehicle.mileage <= :maxMileage) " +
                    "AND (:minBatteryCapacity IS NULL OR l.product.evVehicle.batteryCapacity >= :minBatteryCapacity) " +
                    "AND (:maxBatteryCapacity IS NULL OR l.product.evVehicle.batteryCapacity <= :maxBatteryCapacity) " +
                    "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
                    "AND l.product.evVehicle IS NOT NULL " +
                    "ORDER BY l.createdAt DESC"
    )
    Page<ListingEntity> filterEvListings(
            @Param("year") Integer year,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("type") VehicleType type,
            @Param("brand") String brand,
            @Param("province") String province,
            @Param("district") String district,
            @Param("conditionStatus") VehicleCondition conditionStatus,
            @Param("minMileage") Integer minMileage,
            @Param("maxMileage") Integer maxMileage,
            @Param("minBatteryCapacity") Integer minBatteryCapacity,
            @Param("maxBatteryCapacity") Integer maxBatteryCapacity,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query(
            "SELECT l FROM ListingEntity l " +
                    "JOIN LocationEntity lt ON l.id = lt.listing.id " +
                    "WHERE l.status = 'ACTIVE' " +
                    "AND (:brand IS NULL OR LOWER(l.product.battery.brand) = LOWER(:brand)) " +
                    "AND (:name IS NULL OR LOWER(l.product.battery.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:year IS NULL OR l.product.battery.year = :year) " +
                    "AND (:minYear IS NULL OR l.product.battery.year >= :minYear) " +
                    "AND (:maxYear IS NULL OR l.product.battery.year <= :maxYear) " +
                    "AND (:province IS NULL OR LOWER(lt.province) LIKE LOWER(CONCAT('%', :province, '%'))) " +
                    "AND (:district IS NULL OR LOWER(lt.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
                    "AND (:conditionStatus IS NULL OR l.product.battery.conditionStatus = :conditionStatus) " +
                    "AND (:compatibility IS NULL OR LOWER(l.product.battery.compatibleVehicles) LIKE LOWER(CONCAT('%', :compatibility, '%'))) " +
                    "AND (:minBatteryCapacity IS NULL OR l.product.battery.capacity >= :minBatteryCapacity) " +
                    "AND (:maxBatteryCapacity IS NULL OR l.product.battery.capacity <= :maxBatteryCapacity) " +
                    "AND (:minHealthPercentage IS NULL OR l.product.battery.healthPercentage >= :minHealthPercentage) " +
                    "AND (:maxHealthPercentage IS NULL OR l.product.battery.healthPercentage <= :maxHealthPercentage) " +
                    "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
                    "AND l.product.battery IS NOT NULL " +
                    "ORDER BY l.createdAt DESC"
    )
    Page<ListingEntity> filterBatteryListings(
            @Param("brand") String brand,
            @Param("name") String name,
            @Param("year") Integer year,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("province") String province,
            @Param("district") String district,
            @Param("conditionStatus") BatteryCondition conditionStatus,
            @Param("compatibility") String compatibility,
            @Param("minBatteryCapacity") Integer minBatteryCapacity,
            @Param("maxBatteryCapacity") Integer maxBatteryCapacity,
            @Param("minHealthPercentage") Integer minHealthPercentage,
            @Param("maxHealthPercentage") Integer maxHealthPercentage,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

         @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = :status " +
            "ORDER BY l.createdAt DESC")
         List<ListingEntity> findAllListingsByStatus(@Param("status") ListingStatus status);


        @Query("SELECT l FROM ListingEntity l " +
                        "WHERE l.status = :status AND l.product.evVehicle.type = :type ORDER BY l.createdAt DESC")
        Page<ListingEntity> findByStatusAndVehicleType(@Param("status") ListingStatus status,
                                                       @Param("type") VehicleType type,
                                                       Pageable pageable);

        @Query("SELECT l FROM ListingEntity l WHERE l.product.evVehicle.type = :type ORDER BY l.createdAt DESC")
        List<ListingEntity> findAllByVehicleTypeNoStatus(@Param("type") VehicleType type);

        long countListingEntitiesByStatus(ListingStatus status);

        @Query(
                "SELECT Count(l) FROM ListingEntity l " +
                        "WHERE l.product.evVehicle IS NOT NULL "
        )
        long countListingsHavingEvVehicle();

        @Query(
            "SELECT Count(l) FROM ListingEntity l " +
                    "WHERE l.product.battery IS NOT NULL "
    )
        long countListingsHavingBattery();
}