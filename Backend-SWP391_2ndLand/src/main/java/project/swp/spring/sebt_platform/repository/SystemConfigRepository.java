package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.swp.spring.sebt_platform.model.SystemConfigEntity;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, Long> {

    SystemConfigEntity findByConfigKey(String key);
}
