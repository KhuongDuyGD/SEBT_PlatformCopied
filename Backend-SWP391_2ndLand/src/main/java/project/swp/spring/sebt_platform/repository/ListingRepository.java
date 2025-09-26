package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.model.ListingEntity;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, Long> {

    //List<ListingCartResponseDTO> findByFilters();
}
