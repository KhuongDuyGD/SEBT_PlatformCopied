package project.swp.spring.sebt_platform.pricing.baseline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BaselinePriceService {
    private static final Logger log = LoggerFactory.getLogger(BaselinePriceService.class);
    private final BaselinePriceRepository repository;

    // Patterns (simplified & safe)
    private static final Pattern LINEAR_PCT = Pattern.compile("(?:linear|lin)[^0-9]{0,10}(\\d{1,2})%", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXP_FACTOR = Pattern.compile("(0?\\.\\d{2})\\^age"); // e.g. 0.92^age
    private static final Pattern MAX_DEPR = Pattern.compile("max[^0-9]{0,10}(\\d{1,2})%", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAX_DEPR_ALT = Pattern.compile("(\\d{1,2})% depreciation", Pattern.CASE_INSENSITIVE);

    public BaselinePriceService(BaselinePriceRepository repository) { this.repository = repository; }

    public LookupResult lookup(String brand, String model, String variant, int ageYears) {
        Optional<BaselinePriceEntry> match = repository.findBestMatch(brand, model, variant);
        if (match.isEmpty()) {
            log.debug("Baseline not found brand={} model={} variant={} ageYears={}", brand, model, variant, ageYears);
            return LookupResult.notFound();
        }
        BaselinePriceEntry entry = match.get();
        DepreciationStrategy strategy = parseStrategy(entry.getNotes());
        long depreciated = strategy.apply(entry.getBaselinePrice(), ageYears);
        if (depreciated > entry.getBaselinePrice()) depreciated = entry.getBaselinePrice();
        log.debug("Baseline resolved brand={} model={} variant={} base={} depreciated={} strategy={} rate={} maxDep={}",
                entry.getBrand(), entry.getModel(), entry.getVariant(), entry.getBaselinePrice(), depreciated,
                strategy.getType(), strategy.getRate(), strategy.getMaxDepreciation());
        return LookupResult.found(entry, depreciated, strategy);
    }

    private DepreciationStrategy parseStrategy(String notes) {
        if (notes == null || notes.isBlank()) return DepreciationStrategy.none();
        double maxDep = 0;
        Matcher mMax = MAX_DEPR.matcher(notes);
        if (mMax.find()) {
            maxDep = Integer.parseInt(mMax.group(1)) / 100.0;
        } else {
            Matcher alt = MAX_DEPR_ALT.matcher(notes);
            if (alt.find()) maxDep = Integer.parseInt(alt.group(1)) / 100.0;
        }
        Matcher linear = LINEAR_PCT.matcher(notes);
        if (linear.find()) {
            int pct = Integer.parseInt(linear.group(1));
            return new DepreciationStrategy(DepreciationStrategy.Type.LINEAR, pct / 100.0, maxDep);
        }
        Matcher exp = EXP_FACTOR.matcher(notes);
        if (exp.find()) {
            double factor = Double.parseDouble(exp.group(1));
            if (factor <= 0 || factor >= 1) factor = 0.92; // sanity fallback
            return new DepreciationStrategy(DepreciationStrategy.Type.EXPONENTIAL, factor, maxDep);
        }
        // default gentle linear 7%/year
        return new DepreciationStrategy(DepreciationStrategy.Type.LINEAR, 0.07, maxDep);
    }

    public record LookupResult(boolean found, BaselinePriceEntry entry, long depreciatedPrice, DepreciationStrategy strategy) {
        public static LookupResult notFound() { return new LookupResult(false, null, 0, DepreciationStrategy.none()); }
        public static LookupResult found(BaselinePriceEntry e, long price, DepreciationStrategy s) { return new LookupResult(true, e, price, s); }
    }
}
