package project.swp.spring.sebt_platform.pricing.baseline;

/**
 * Đại diện cho chiến lược khấu hao (giảm giá trị) của một tài sản theo thời gian.
 *
 * @param type Loại khấu hao (LINEAR, EXPONENTIAL, NONE).
 * @param rate Tỷ lệ áp dụng (LINEAR: %/năm; EXPONENTIAL: hệ số/năm).
 * @param maxDepreciation Tỷ lệ khấu hao tối đa (phần trăm 0..1) được phép.
 */
public record DepreciationStrategy(Type type, double rate, double maxDepreciation) {
    public int getMaxDepreciation() {return 0;
    }

    public enum Type { LINEAR, EXPONENTIAL, NONE }

    /**
     * Áp dụng chiến lược khấu hao vào giá cơ sở (baseline) dựa trên số năm sử dụng (ageYears).
     *
     * @param baseline Giá trị ban đầu (mới).
     * @param ageYears Số năm tuổi của tài sản.
     * @return Giá trị đã khấu hao (đã làm tròn).
     */
    public long apply(long baseline, int ageYears) {
        if (ageYears <= 0 || type == Type.NONE) return baseline;
        double value = baseline;

        switch (type) {
            case LINEAR -> value = applyLinear(baseline, ageYears);
            case EXPONENTIAL -> value = applyExponential(baseline, ageYears);
            case NONE -> {} // Không thay đổi
        }

        // Đảm bảo giá trị không dưới 1
        return Math.max(1, Math.round(value));
    }

    private double applyLinear(long baseline, int ageYears) {
        // Mức khấu hao tích lũy: min(số năm * tỷ lệ, tối đa 95% nếu không có maxDep cụ thể)
        double maxDepCap = (maxDepreciation > 0) ? maxDepreciation : 0.95;
        double depreciation = Math.min(ageYears * rate, maxDepCap);
        return baseline * (1.0 - depreciation);
    }

    private double applyExponential(long baseline, int ageYears) {
        // Giá trị sau khi khấu hao lũy thừa
        double value = baseline * Math.pow(rate, ageYears);

        if (maxDepreciation > 0) {
            // Đảm bảo giá trị không dưới mức sàn (baseline * (1 - maxDepreciation))
            double floor = baseline * (1 - maxDepreciation);
            value = Math.max(value, floor);
        }
        return value;
    }

    /**
     * Tạo một chiến lược khấu hao rỗng (NONE).
     */
    public static DepreciationStrategy none() { return new DepreciationStrategy(Type.NONE, 0, 0); }
}