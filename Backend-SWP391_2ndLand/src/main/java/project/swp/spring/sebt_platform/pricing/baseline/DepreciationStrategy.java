package project.swp.spring.sebt_platform.pricing.baseline;

public class DepreciationStrategy {
    public enum Type { LINEAR, EXPONENTIAL, NONE }
    private final Type type;
    private final double rate; // LINEAR: %/year; EXPONENTIAL: factor per year
    private final double maxDepreciation; // fraction 0..1 cap of total depreciation

    public DepreciationStrategy(Type type, double rate, double maxDepreciation) {
        this.type = type; this.rate = rate; this.maxDepreciation = maxDepreciation;
    }
    public Type getType() { return type; }
    public double getRate() { return rate; }
    public double getMaxDepreciation() { return maxDepreciation; }

    public long apply(long baseline, int ageYears) {
        if (ageYears <= 0 || type == Type.NONE) return baseline;
        double value = baseline;
        switch (type) {
            case LINEAR -> {
                double depreciation = Math.min(ageYears * rate, maxDepreciation > 0 ? maxDepreciation : 0.95);
                value = baseline * (1.0 - depreciation);
            }
            case EXPONENTIAL -> {
                double factor = Math.pow(rate, ageYears);
                value = baseline * factor;
                if (maxDepreciation > 0) {
                    double floor = baseline * (1 - maxDepreciation);
                    value = Math.max(value, floor);
                }
            }
            case NONE -> {
                // no change
            }
        }
        return Math.max(1, Math.round(value));
    }

    public static DepreciationStrategy none() { return new DepreciationStrategy(Type.NONE, 0, 0); }
}
