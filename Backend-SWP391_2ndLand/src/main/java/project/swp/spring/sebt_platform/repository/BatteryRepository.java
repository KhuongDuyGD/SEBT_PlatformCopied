package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.model.BatteryEntity;

@Repository
public interface BatteryRepository extends JpaRepository<BatteryEntity, Long> {
}
