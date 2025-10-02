package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.swp.spring.sebt_platform.model.FavoriteEntity;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    FavoriteEntity findByUserIdAndListingId(Long userId, Long listingId);
}
