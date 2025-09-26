package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.model.ListingImageEntity;

import java.util.List;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImageEntity, Long> {

    @Query("SELECT li FROM ListingImageEntity li WHERE li.listing.id = :listingId ORDER BY li.displayOrder ASC")
    List<ListingImageEntity> findByListingIdOrderByDisplayOrder(@Param("listingId") Long listingId);

    @Query("DELETE FROM ListingImageEntity li WHERE li.listing.id = :listingId")
    void deleteByListingId(@Param("listingId") Long listingId);
}
