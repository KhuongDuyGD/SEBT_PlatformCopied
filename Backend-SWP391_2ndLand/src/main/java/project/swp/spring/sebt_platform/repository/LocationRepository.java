package project.swp.spring.sebt_platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import project.swp.spring.sebt_platform.model.LocationEntity;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    LocationEntity findByListingId(Long listingId);
    
    @Query("SELECT DISTINCT l.province FROM LocationEntity l ORDER BY l.province ASC")
    List<String> findDistinctProvinces();
    
    @Query("SELECT DISTINCT l.district FROM LocationEntity l ORDER BY l.district ASC")
    List<String> findDistinctDistricts();
    
    @Query("SELECT DISTINCT l.district FROM LocationEntity l WHERE l.province = :province ORDER BY l.district ASC")
    List<String> findDistinctDistrictsByProvince(String province);
}
