package project.swp.spring.sebt_platform.service.impl;

import org.springframework.stereotype.Component;
import project.swp.spring.sebt_platform.service.ListingFeePolicy;

import java.math.BigDecimal;

/**
 * Default fee policy:
 *  - Battery only listing: 50,000 VND
 *  - EV listing: if price < 500,000,000 VND => 100,000; else 200,000 VND
 *  - Mixed (if both present) choose higher rule (treat as EV)
 */
@Component
public class ListingFeePolicyImpl implements ListingFeePolicy {

    private static final BigDecimal BATTERY_FEE = BigDecimal.valueOf(50_000);
    private static final BigDecimal EV_THRESHOLD = BigDecimal.valueOf(500_000_000L);
    private static final BigDecimal EV_LOW_FEE = BigDecimal.valueOf(100_000);
    private static final BigDecimal EV_HIGH_FEE = BigDecimal.valueOf(200_000);

    @Override
    public BigDecimal computeFee(ListingContext ctx) {
        boolean isEv = ctx.hasEv();
        boolean isBattery = ctx.hasBattery();
        if (isEv) {
            if (ctx.price() == null) return EV_LOW_FEE; // fallback
            return ctx.price().compareTo(EV_THRESHOLD) < 0 ? EV_LOW_FEE : EV_HIGH_FEE;
        }
        if (isBattery) {
            return BATTERY_FEE;
        }
        // Fallback if none detected (should not happen) treat as battery tier
        return BATTERY_FEE;
    }
}
