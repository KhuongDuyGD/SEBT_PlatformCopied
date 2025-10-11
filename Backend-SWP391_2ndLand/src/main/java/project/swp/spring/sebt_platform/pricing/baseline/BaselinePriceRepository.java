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

@Component
public class BaselinePriceRepository {
    private static final Logger log = LoggerFactory.getLogger(BaselinePriceRepository.class);
    private final Map<String, List<BaselinePriceEntry>> byBrand = new ConcurrentHashMap<>();

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
        }
    }

    public Optional<BaselinePriceEntry> findBestMatch(String brand, String model, String variant) {
        if (brand == null) return Optional.empty();
        List<BaselinePriceEntry> list = byBrand.get(normalize(brand));
        if (list == null) return Optional.empty();
        String modelNorm = normalize(model);
        String variantNorm = normalize(variant);
        String modelCompact = compact(modelNorm);

        // 1. Exact model + (optional) variant match
        Optional<BaselinePriceEntry> exact = list.stream()
                .filter(e -> modelNorm.equals(normalize(e.getModel())) &&
                        (variantNorm.isBlank() || variantNorm.equals(normalize(e.getVariant()))))
                .findFirst();
        if (exact.isPresent()) return exact;

        // 2. Compact equality (remove spaces & non-alnum) e.g. "vf8" == "vf 8"
        Optional<BaselinePriceEntry> compactEq = list.stream()
                .filter(e -> modelCompact.equals(compact(normalize(e.getModel()))))
                .findFirst();
        if (compactEq.isPresent()) return compactEq;

        // 3. Contains (normalized or compact contains)
        Optional<BaselinePriceEntry> contains = list.stream().filter(e -> {
            String em = normalize(e.getModel());
            return modelNorm.contains(em) || em.contains(modelNorm) ||
                    modelCompact.contains(compact(em)) || compact(em).contains(modelCompact);
        }).findFirst();
        if (contains.isPresent()) return contains;

        // 4. Fallback: first (stable) to keep prior behaviour, but log once
        if (!list.isEmpty()) {
            log.debug("Baseline fallback first entry for brand={} modelNorm={} modelCompact={}", brand, modelNorm, modelCompact);
            return Optional.of(list.get(0));
        }
        return Optional.empty();
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private String compact(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-z0-9]", "");
    }
}
