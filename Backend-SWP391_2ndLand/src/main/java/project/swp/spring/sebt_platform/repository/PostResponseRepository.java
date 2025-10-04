package project.swp.spring.sebt_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.model.PostResponseEntity;

@Repository
public interface PostResponseRepository extends JpaRepository<PostResponseEntity, Long> {

    @Query(
        "SELECT p " +
        "FROM PostResponseEntity p " +
        "WHERE (:sellerId IS NULL OR p.postRequest.listing.seller.id = :sellerId)"
    )
    Page<PostResponseEntity> findAllBySellerId( Long sellerId, Pageable pageable);
}
