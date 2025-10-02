package project.swp.spring.sebt_platform.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.VehicleType;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, Long> {

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

    List<ListingEntity> findByBrand(String brand);

    List<ListingEntity> findByYear( Integer year);

    // Query tất cả active listings (khi không có filter nào)
    @Query("SELECT l FROM ListingEntity l " +
           "WHERE l.status = 'ACTIVE' " +
           "ORDER BY l.createdAt DESC")
    List<ListingEntity> findAllActiveListings();
}