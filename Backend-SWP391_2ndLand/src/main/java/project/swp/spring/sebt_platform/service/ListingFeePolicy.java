package project.swp.spring.sebt_platform.service;

import java.math.BigDecimal;

/**
 * Policy interface to compute listing publication fee based on product type & price.
 */
public interface ListingFeePolicy {

    BigDecimal computeFee(ListingContext ctx);

    record ListingContext(boolean hasEv, boolean hasBattery, BigDecimal price) {}
}
