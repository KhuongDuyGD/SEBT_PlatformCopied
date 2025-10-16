package project.swp.spring.sebt_platform.model.enums;

/**
 * Purpose classification for wallet ledger entries.
 * Scope simplified: only top-up and listing related fees now.
 */
public enum WalletPurpose {
    TOP_UP,
    TOP_UP_REFUND,
    LISTING_FEE,
    LISTING_FEE_REFUND,
    PRICING_FEE,
    ADJUST
}
