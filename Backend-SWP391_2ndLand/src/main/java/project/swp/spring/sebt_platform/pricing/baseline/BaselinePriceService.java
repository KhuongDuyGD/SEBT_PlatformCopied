package project.swp.spring.sebt_platform.pricing.baseline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dịch vụ tra cứu giá cơ sở (baseline) và áp dụng chiến lược khấu hao dựa trên tuổi xe (ageYears).
 */
@Service
public class BaselinePriceService {
    private static final Logger log = LoggerFactory.getLogger(BaselinePriceService.class);
    private final BaselinePriceRepository repository;

    // Các Pattern Regex để phân tích Ghi chú (Notes)
    private static final Pattern LINEAR_PCT = Pattern.compile("(?:linear|lin)[^0-9]{0,10}(\\d{1,2})%", Pattern.CASE_INSENSITIVE); // Tìm linear X%
    private static final Pattern EXP_FACTOR = Pattern.compile("(0?\\.\\d{2})\\^age"); // Tìm 0.XX^age
    private static final Pattern MAX_DEPR = Pattern.compile("max[^0-9]{0,10}(\\d{1,2})%", Pattern.CASE_INSENSITIVE); // Tìm max X%
    private static final Pattern MAX_DEPR_ALT = Pattern.compile("(\\d{1,2})% depreciation", Pattern.CASE_INSENSITIVE); // Tìm X% depreciation

    /**
     * Record đại diện cho kết quả tra cứu giá cơ sở.
     *
     * @param found Đã tìm thấy mục nhập baseline hay không.
     * @param entry Mục nhập giá cơ sở gốc.
     * @param depreciatedPrice Giá đã khấu hao (nếu có).
     * @param strategy Chiến lược khấu hao được áp dụng.
     */
    public record LookupResult(boolean found, BaselinePriceEntry entry, long depreciatedPrice, DepreciationStrategy strategy) {
        public static LookupResult notFound() { return new LookupResult(false, null, 0, DepreciationStrategy.none()); }
        public static LookupResult found(BaselinePriceEntry e, long price, DepreciationStrategy s) { return new LookupResult(true, e, price, s); }
    }

    public BaselinePriceService(BaselinePriceRepository repository) { this.repository = repository; }

    /**
     * Tra cứu giá cơ sở và tính toán giá trị đã khấu hao.
     *
     * @param brand Thương hiệu.
     * @param model Mẫu xe.
     * @param variant Biến thể.
     * @param ageYears Tuổi xe (số năm).
     * @return {@link LookupResult} chứa giá cơ sở, giá đã khấu hao và chiến lược.
     */
    public LookupResult lookup(String brand, String model, String variant, int ageYears) {
        Optional<BaselinePriceEntry> match = repository.findBestMatch(brand, model, variant);
        if (match.isEmpty()) {
            log.debug("Baseline not found brand={} model={} variant={} ageYears={}", brand, model, variant, ageYears);
            return LookupResult.notFound();
        }

        BaselinePriceEntry entry = match.get();
        DepreciationStrategy strategy = parseStrategy(entry.getNotes());
        long depreciated = strategy.apply(entry.getBaselinePrice(), ageYears);

        // Đảm bảo giá trị đã khấu hao không vượt quá giá baseline ban đầu
        if (depreciated > entry.getBaselinePrice()) depreciated = entry.getBaselinePrice();

        log.debug("Baseline resolved brand={} model={} variant={} base={} depreciated={} strategy={} rate={} maxDep={}",
                entry.getBrand(), entry.getModel(), entry.getVariant(), entry.getBaselinePrice(), depreciated,
                strategy.type(), strategy.rate(), strategy.maxDepreciation()); // Dùng getter của Record
        return LookupResult.found(entry, depreciated, strategy);
    }

    /**
     * Phân tích chuỗi ghi chú (notes) để xác định chiến lược khấu hao (Type, Rate, MaxDep).
     *
     * @param notes Chuỗi ghi chú từ JSON.
     * @return {@link DepreciationStrategy} tương ứng.
     */
    private DepreciationStrategy parseStrategy(String notes) {
        if (notes == null || notes.isBlank()) return DepreciationStrategy.none();
        double maxDep = 0;

        // 1. Phân tích Max Depreciation
        Matcher mMax = MAX_DEPR.matcher(notes);
        if (mMax.find()) {
            maxDep = Integer.parseInt(mMax.group(1)) / 100.0;
        } else {
            Matcher alt = MAX_DEPR_ALT.matcher(notes);
            if (alt.find()) maxDep = Integer.parseInt(alt.group(1)) / 100.0;
        }

        // 2. Phân tích Linear Strategy
        Matcher linear = LINEAR_PCT.matcher(notes);
        if (linear.find()) {
            int pct = Integer.parseInt(linear.group(1));
            return new DepreciationStrategy(DepreciationStrategy.Type.LINEAR, pct / 100.0, maxDep);
        }

        // 3. Phân tích Exponential Strategy
        Matcher exp = EXP_FACTOR.matcher(notes);
        if (exp.find()) {
            double factor = Double.parseDouble(exp.group(1));
            // Kiểm tra tính hợp lệ của hệ số: phải nằm trong (0, 1)
            if (factor <= 0 || factor >= 1) factor = 0.92; // Fallback an toàn
            return new DepreciationStrategy(DepreciationStrategy.Type.EXPONENTIAL, factor, maxDep);
        }

        // 4. Default: Nếu không tìm thấy, sử dụng khấu hao Linear nhẹ 7%/năm
        return new DepreciationStrategy(DepreciationStrategy.Type.LINEAR, 0.07, maxDep);
    }
}