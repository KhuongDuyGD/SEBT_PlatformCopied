package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.repository.SystemConfigRepository;
import project.swp.spring.sebt_platform.service.FeePolicyService;

import java.math.BigDecimal;

/**
 * Default fee policy:
 *  - Battery only listing: 50,000 VND
 *  - EV listing: if price < 500,000,000 VND => 100,000; else 200,000 VND
 *  - Mixed (if both present) choose higher rule (treat as EV)
 */
@Service
public class FeePolicyServiceImpl implements FeePolicyService {

    SystemConfigRepository systemConfig;

    @Autowired
    public FeePolicyServiceImpl(SystemConfigRepository systemConfig) {
        this.systemConfig = systemConfig;
    }

    @Override
    public BigDecimal computeListingFee(boolean hasEv, boolean hasBattery, Long price) {

        long BATTERY_FEE = Long.parseLong(systemConfig.findByConfigKey("BATTERY_FEE").getConfigValue());
        long EV_THRESHOLD = Long.parseLong(systemConfig.findByConfigKey("EV_THRESHOLD").getConfigValue());
        long EV_LOW_FEE = Long.parseLong(systemConfig.findByConfigKey("EV_LOW_FEE").getConfigValue());
        long EV_HIGH_FEE = Long.parseLong(systemConfig.findByConfigKey("EV_HIGH_FEE").getConfigValue());

        if (hasEv) {
            if (price == null) return BigDecimal.valueOf(EV_LOW_FEE) ; // fallback
            return price.compareTo(EV_THRESHOLD) < 0 ? BigDecimal.valueOf(EV_LOW_FEE) : BigDecimal.valueOf(EV_HIGH_FEE);
        }
        // Fallback if none detected (should not happen) treat as battery tier
        return BigDecimal.valueOf(BATTERY_FEE);
    }

}
