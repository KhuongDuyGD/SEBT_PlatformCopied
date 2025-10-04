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

import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.VehicleType;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, Long>, JpaSpecificationExecutor<ListingEntity> {

    ListingEntity findById(long id);

    List<ListingEntity> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    // Query tìm kiếm theo keyword với pagination
    Page<ListingEntity> findByTitleContainingIgnoreCaseAndStatus(String keyword, ListingStatus status, Pageable pageable);

    // Query tìm car listings với pagination
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = :status " +
            "AND l.product.evVehicle IS NOT NULL " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> findCarListingsByStatus(@Param("status") ListingStatus status, Pageable pageable);

    // Query tìm battery listings với pagination
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = :status " +
            "AND l.product.battery IS NOT NULL " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> findBatteryListingsByStatus(@Param("status") ListingStatus status, Pageable pageable);


    // Query tìm listings theo seller với pagination
    Page<ListingEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    // Query theo title
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<ListingEntity> findByTitleContaining(@Param("title") String title);

    // Sửa: Query theo brand trong EvVehicle hoặc Battery
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND (l.product.evVehicle.brand = :brand OR l.product.battery.brand = :brand)")
    List<ListingEntity> findByBrand(@Param("brand") String brand);

    // Sửa: Query theo vehicleType trong EvVehicle
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND l.product.evVehicle.type = :vehicleType")
    List<ListingEntity> findByVehicleType(@Param("vehicleType") VehicleType vehicleType);

    // Sửa: Đổi từ Double sang BigDecimal và thêm @Query
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND l.price BETWEEN :minPrice AND :maxPrice")
    List<ListingEntity> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Query cho min price only
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND l.price >= :minPrice")
    List<ListingEntity> findByPriceGreaterThanEqual(@Param("minPrice") BigDecimal minPrice);

    // Query cho max price only
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND l.price <= :maxPrice")
    List<ListingEntity> findByPriceLessThanEqual(@Param("maxPrice") BigDecimal maxPrice);

    // Sửa: Query theo year trong EvVehicle
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND l.product.evVehicle.year = :year")
    List<ListingEntity> findByYear(@Param("year") Integer year);

    // Query tất cả active listings (khi không có filter nào)
    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "ORDER BY l.createdAt DESC")
    List<ListingEntity> findAllActiveListings();

        // Pagination-friendly query when only filtering by vehicle type
        @Query("SELECT l FROM ListingEntity l " +
                        "WHERE l.status = :status AND l.product.evVehicle.type = :type ORDER BY l.createdAt DESC")
        Page<ListingEntity> findByStatusAndVehicleType(@Param("status") ListingStatus status, @Param("type") VehicleType type, Pageable pageable);

        // Diagnostic: fetch all listings for a vehicle type regardless of status (debug only)
        @Query("SELECT l FROM ListingEntity l WHERE l.product.evVehicle.type = :type ORDER BY l.createdAt DESC")
        List<ListingEntity> findAllByVehicleTypeNoStatus(@Param("type") VehicleType type);
}