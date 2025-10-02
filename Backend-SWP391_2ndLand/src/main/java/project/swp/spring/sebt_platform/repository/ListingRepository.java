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

//   List<ListingEntity> findByKeywordAndStatus(String keyword, ListingStatus status);

    ListingEntity findById(long id);

    List<ListingEntity> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    @Query (
        "SELECT l FROM ListingEntity l" +
                " WHERE l.status = :status" +
                " AND l.product.evVehicle IS NOT NULL" +
                " ORDER BY l.createdAt DESC"
    )
    List<ListingEntity> findCarListingsByStatus(@Param("status") ListingStatus status,
                                                Pageable pageable);

    @Query (
        "SELECT l FROM ListingEntity l" +
                " WHERE l.status = :status" +
                " AND l.product.battery IS NOT NULL" +
                " ORDER BY l.createdAt DESC"
    )
    List<ListingEntity> findBatteryListingsByStatus(@Param("status") ListingStatus status,
                                                Pageable pageable);


    List<ListingEntity> findAllActiveByStatus(ListingStatus status,
                                              Pageable pageable);

    // Lấy listing theo seller
    List<ListingEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

}
