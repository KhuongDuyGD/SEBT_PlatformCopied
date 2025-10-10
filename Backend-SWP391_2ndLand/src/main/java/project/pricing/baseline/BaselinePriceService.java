package project.pricing.baseline;

/** Legacy placeholder (real service relocated). */
@Deprecated
public class BaselinePriceService {
    public record LookupResult(boolean found, BaselinePriceEntry entry, long depreciatedPrice, DepreciationStrategy strategy) {
        public static LookupResult notFound() { return new LookupResult(false, null, 0, DepreciationStrategy.none()); }
    }
    public LookupResult lookup(String brand, String model, String variant, int ageYears) { return LookupResult.notFound(); }
}
