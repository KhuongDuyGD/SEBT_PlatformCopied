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

    // Tìm kiếm listing theo từ khóa trong title hoặc description
    @Query("SELECT l FROM ListingEntity l WHERE " +
           "(LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "l.status = :status")
    List<ListingEntity> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                               @Param("status") ListingStatus status);

    // Lấy tất cả listing đang active, sắp xếp theo ngày tạo mới nhất
    List<ListingEntity> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    // Lấy listing cho xe (car listings) - chỉ lấy những listing có sản phẩm là xe
    @Query("SELECT l FROM ListingEntity l " +
           "JOIN l.product p " +
           "JOIN p.evVehicle ev " +
           "WHERE l.status = :status AND ev.type = :vehicleType " +
           "ORDER BY l.createdAt DESC")
    List<ListingEntity> findCarListingsByStatus(@Param("status") ListingStatus status, 
                                                @Param("vehicleType") VehicleType vehicleType);

    // Lấy pin listings - chỉ lấy những listing có sản phẩm là pin
    @Query("SELECT l FROM ListingEntity l " +
           "JOIN l.product p " +
           "WHERE l.status = :status AND p.battery IS NOT NULL " +
           "ORDER BY l.createdAt DESC")
    List<ListingEntity> findPinListingsByStatus(@Param("status") ListingStatus status);

    // Lấy listing theo seller
    List<ListingEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    // Tìm kiếm với phân trang
    Page<ListingEntity> findByStatusOrderByCreatedAtDesc(ListingStatus status, Pageable pageable);

    // Lấy listing theo brand của sản phẩm
    @Query("SELECT l FROM ListingEntity l " +
           "JOIN l.product p " +
           "LEFT JOIN p.evVehicle ev " +
           "LEFT JOIN p.battery b " +
           "WHERE l.status = :status AND " +
           "(LOWER(ev.brand) LIKE LOWER(CONCAT('%', :brand, '%')) OR " +
           "LOWER(b.brand) LIKE LOWER(CONCAT('%', :brand, '%')))")
    List<ListingEntity> findByProductBrandAndStatus(@Param("brand") String brand, 
                                                    @Param("status") ListingStatus status);
}
