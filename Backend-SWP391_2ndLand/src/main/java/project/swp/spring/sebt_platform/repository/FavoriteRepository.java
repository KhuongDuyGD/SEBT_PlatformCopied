package project.swp.spring.sebt_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import project.swp.spring.sebt_platform.model.FavoriteEntity;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    FavoriteEntity findByUserIdAndListingId(Long userId, Long listingId);

    // Batch find all favorites for user across a set of listing IDs
    @Query("SELECT f.listing.id FROM FavoriteEntity f WHERE f.user.id = :userId AND f.listing.id IN :listingIds")
    List<Long> findFavoritedListingIds(@Param("userId") Long userId, @Param("listingIds") List<Long> listingIds);

    Page<FavoriteEntity> findByUserId(Long userId, Pageable pageable);
}
