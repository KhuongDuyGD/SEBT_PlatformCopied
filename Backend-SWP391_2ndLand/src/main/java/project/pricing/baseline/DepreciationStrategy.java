package project.pricing.baseline;

/**
 * Legacy placeholder kept only to avoid breaking existing imports.
 * Real implementation moved to package: project.swp.spring.sebt_platform.pricing.baseline
 */
@Deprecated
public class DepreciationStrategy {
    public enum Type { NONE }
    public DepreciationStrategy(Type t, double r, double m) {}
    public Type getType() { return Type.NONE; }
    public double getRate() { return 0; }
    public double getMaxDepreciation() { return 0; }
    public long apply(long baseline, int ageYears) { return baseline; }
    public static DepreciationStrategy none() { return new DepreciationStrategy(Type.NONE,0,0); }
}
