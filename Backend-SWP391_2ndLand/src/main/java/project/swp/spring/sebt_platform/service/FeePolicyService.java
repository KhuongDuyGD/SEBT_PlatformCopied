package project.swp.spring.sebt_platform.service;

import java.math.BigDecimal;

/**
 * Policy interface to compute listing publication fee based on product type & price.
 */
public interface FeePolicyService {

    BigDecimal computeListingFee(boolean hasEv, boolean hasBattery, Long price);

}
