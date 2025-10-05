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

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND (:year IS NULL OR l.product.evVehicle.year = :year) " +
            "AND (:type IS NULL OR l.product.evVehicle.type = :type) " +
            "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
            "AND l.product.evVehicle IS NOT NULL " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> filterEvListings(
            @Param("year") Integer year,
            @Param("type") VehicleType type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND (:year IS NULL OR l.product.evVehicle.year = :year) " +
            "AND (:type IS NULL OR l.product.evVehicle.type = :type) " +
            "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
            "AND l.product.battery IS NOT NULL " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> filterBatteryListings(
            @Param("year") Integer year,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT l FROM ListingEntity l " +
            "WHERE l.status = 'ACTIVE' " +
            "ORDER BY l.createdAt DESC")
    List<ListingEntity> findAllActiveListings();


        @Query("SELECT l FROM ListingEntity l " +
                        "WHERE l.status = :status AND l.product.evVehicle.type = :type ORDER BY l.createdAt DESC")
        Page<ListingEntity> findByStatusAndVehicleType(@Param("status") ListingStatus status, @Param("type") VehicleType type, Pageable pageable);


        @Query("SELECT l FROM ListingEntity l WHERE l.product.evVehicle.type = :type ORDER BY l.createdAt DESC")
        List<ListingEntity> findAllByVehicleTypeNoStatus(@Param("type") VehicleType type);
}