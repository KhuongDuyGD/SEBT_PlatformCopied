package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, Long> {

    //List<ListingCartResponseDTO> findByFilters();
}
