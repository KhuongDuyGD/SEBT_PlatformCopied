package project.swp.spring.sebt_platform.pricing.baseline;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository lưu trữ và quản lý dữ liệu giá cơ sở (BaselinePriceEntry).
 * Dữ liệu được tải từ file JSON và lưu trữ trong bộ nhớ để tra cứu nhanh.
 */
@Component
public class BaselinePriceRepository {
    private static final Logger log = LoggerFactory.getLogger(BaselinePriceRepository.class);
    // Lưu trữ dữ liệu theo Brand đã được chuẩn hóa để tra cứu hiệu suất cao.
    private final Map<String, List<BaselinePriceEntry>> byBrand = new ConcurrentHashMap<>();

    /**
     * Tải dữ liệu giá cơ sở từ file JSON sau khi Bean được khởi tạo (Post-Construct).
     */
    @PostConstruct
    public void load() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource("pricing/baseline-prices.json").getInputStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            BaselinePriceData data = mapper.readValue(json, BaselinePriceData.class);
            if (data.getBaselinePrices() != null) {
                data.getBaselinePrices().forEach(entry -> {
                    String brandKey = normalize(entry.getBrand());
                    byBrand.computeIfAbsent(brandKey, k -> new ArrayList<>()).add(entry);
                });
                log.info("Loaded {} baseline price entries for {} brands", data.getBaselinePrices().size(), byBrand.size());
            } else {
                log.warn("No baselinePrices array found in JSON");
            }
        } catch (IOException e) {
            log.error("Failed to load baseline-prices.json", e);
            // Xử lý lỗi nghiêm trọng: Có thể throw RuntimeException nếu dữ liệu baseline là bắt buộc
        }
    }

    /**
     * Tìm kiếm mục giá cơ sở phù hợp nhất dựa trên Brand, Model, và Variant.
     * Áp dụng logic so khớp (Matching) nhiều bước:
     * 1. Exact Model + Variant.
     * 2. Compact Model (xoá khoảng trắng).
     * 3. Contains (chứa tên Model).
     * 4. Fallback (trả về mục đầu tiên của Brand nếu có).
     *
     * @param brand Thương hiệu xe/pin.
     * @param model Mẫu xe/pin.
     * @param variant Biến thể/phiên bản cụ thể.
     * @return Optional chứa {@link BaselinePriceEntry} phù hợp nhất.
     */
    public Optional<BaselinePriceEntry> findBestMatch(String brand, String model, String variant) {
        if (brand == null) return Optional.empty();
        List<BaselinePriceEntry> list = byBrand.get(normalize(brand));
        if (list == null) return Optional.empty(); // Không tìm thấy thương hiệu

        String modelNorm = normalize(model);
        String variantNorm = normalize(variant);
        String modelCompact = compact(modelNorm);

        // 1. So khớp Chính xác Model và Variant (nếu Variant không trống)
        Optional<BaselinePriceEntry> exact = list.stream()
                .filter(e -> modelNorm.equals(normalize(e.getModel())) &&
                        (variantNorm.isBlank() || variantNorm.equals(normalize(e.getVariant()))))
                .findFirst();
        if (exact.isPresent()) return exact;

        // 2. So khớp Compact (loại bỏ khoảng trắng/ký tự không cần thiết)
        Optional<BaselinePriceEntry> compactEq = list.stream()
                .filter(e -> modelCompact.equals(compact(normalize(e.getModel()))))
                .findFirst();
        if (compactEq.isPresent()) return compactEq;

        // 3. So khớp Chứa (Model nhập vào chứa Model trong DB, hoặc ngược lại)
        Optional<BaselinePriceEntry> contains = list.stream().filter(e -> {
            String em = normalize(e.getModel());
            return modelNorm.contains(em) || em.contains(modelNorm) ||
                    modelCompact.contains(compact(em)) || compact(em).contains(modelCompact);
        }).findFirst();
        if (contains.isPresent()) return contains;

        // 4. Fallback: Trả về mục đầu tiên cho Brand đó (hành vi cũ)
        if (!list.isEmpty()) {
            log.debug("Baseline fallback to first entry for brand={} modelNorm={} modelCompact={}", brand, modelNorm, modelCompact);
            // Ghi chú: Logic này không lý tưởng nhưng được giữ lại để tương thích với hành vi cũ
            return Optional.of(list.get(0));
        }

        return Optional.empty();
    }

    /** Chuẩn hóa chuỗi: lowercase, trim, thay thế multiple spaces bằng single space. */
    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    /** Nén chuỗi: xóa tất cả ký tự không phải chữ cái/số. */
    private String compact(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-z0-9]", "");
    }
}