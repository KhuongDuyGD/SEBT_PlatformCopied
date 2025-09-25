package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.swp.spring.sebt_platform.model.VerifyPinsEntity;

public interface VerifyPinsRepository extends JpaRepository<VerifyPinsEntity,Long> {

    VerifyPinsEntity findByUser_Id(Long userId);

}
