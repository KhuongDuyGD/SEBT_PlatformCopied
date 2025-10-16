package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.swp.spring.sebt_platform.model.SystemConfigEntity;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, Long> {

    SystemConfigEntity findByConfigKey(String key);

    @Query("SELECT s FROM SystemConfigEntity s " +
            "WHERE s.configKey = 'POST_LISTING_NORMAL_FEE' ")
    SystemConfigEntity findByNormalFee();

    @Query("SELECT s FROM SystemConfigEntity s " +
            "WHERE s.configKey = 'POST_LISTING_PREMIUM_FEE' ")
    SystemConfigEntity findByPremiumFee();

    @Query("SELECT s FROM SystemConfigEntity s " +
            "WHERE s.configKey = 'PRICING_FEE' ")
    SystemConfigEntity findByPricingFee();
}
