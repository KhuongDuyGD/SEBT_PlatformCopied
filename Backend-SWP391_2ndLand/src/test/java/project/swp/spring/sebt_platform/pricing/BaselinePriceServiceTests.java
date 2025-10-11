package project.swp.spring.sebt_platform.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceData;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceEntry;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceRepository;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceService;
import project.swp.spring.sebt_platform.pricing.baseline.BaselinePriceService.LookupResult;
import project.swp.spring.sebt_platform.pricing.baseline.DepreciationStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple sanity tests for BaselinePriceService lookup + depreciation strategy.
 * These tests assume the JSON dataset contains an entry for VinFast VF8 Plus.
 */
class BaselinePriceServiceTests {
    private BaselinePriceService newService() throws Exception {
        // Manual load JSON similar to repository behavior
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource("pricing/baseline-prices.json").getInputStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            BaselinePriceData data = mapper.readValue(json, BaselinePriceData.class);
            // Inline lightweight repository implementation
            BaselinePriceRepository repo = new BaselinePriceRepository();
            // Reflectively inject data (simplest: mimic internal map population)
            java.lang.reflect.Field byBrand = BaselinePriceRepository.class.getDeclaredField("byBrand");
            byBrand.setAccessible(true);
            java.util.Map<String, java.util.List<BaselinePriceEntry>> map = new java.util.concurrent.ConcurrentHashMap<>();
            if (data.getBaselinePrices() != null) {
                for (BaselinePriceEntry e : data.getBaselinePrices()) {
                    String brandKey = e.getBrand().toLowerCase().trim().replaceAll("\\s+"," ");
                    map.computeIfAbsent(brandKey,k->new java.util.ArrayList<>()).add(e);
                }
            }
            byBrand.set(repo, map);
            return new BaselinePriceService(repo);
        }
    }

    @Test
    @DisplayName("VF8 Plus age=0 should return baseline with strategy present")
    void vf8PlusAge0() throws Exception {
        BaselinePriceService baselinePriceService = newService();
        LookupResult lr = baselinePriceService.lookup("VinFast", "VF8 Plus", null, 0);
        assertTrue(lr.found(), "Expected VF8 Plus baseline to be found");
        assertNotNull(lr.entry(), "Entry must not be null");
        assertTrue(lr.entry().getBaselinePrice() > 0, "Baseline price > 0");
        assertNotNull(lr.strategy(), "Strategy should not be null (at least NONE)");
    }

    @Test
    @DisplayName("VF8 Plus age=2 depreciation should reduce or clamp within maxDep")
    void vf8PlusAge2Depreciation() throws Exception {
        BaselinePriceService baselinePriceService = newService();
        int age = 2;
        LookupResult lr0 = baselinePriceService.lookup("VinFast", "VF8 Plus", null, 0);
        LookupResult lr2 = baselinePriceService.lookup("VinFast", "VF8 Plus", null, age);
        assertTrue(lr0.found() && lr2.found(), "Both age=0 and age>0 lookups must be found");
        long base = lr0.entry().getBaselinePrice();
        long dep = lr2.depreciatedPrice();
        assertTrue(dep <= base, "Depreciated price should be <= baseline");
        DepreciationStrategy strategy = lr2.strategy();
        if (strategy.getMaxDepreciation() > 0) {
            long minAllowed = Math.round(base * (1 - strategy.getMaxDepreciation()));
            assertTrue(dep >= minAllowed, "Depreciated price should not exceed max depreciation cap");
        }
    }

    @Test
    @DisplayName("Unknown model should return not found")
    void unknownModel() throws Exception {
        BaselinePriceService baselinePriceService = newService();
        LookupResult lr = baselinePriceService.lookup("NoBrand", "Imaginary X", null, 1);
        assertFalse(lr.found(), "Should not find an imaginary model");
    }
}
