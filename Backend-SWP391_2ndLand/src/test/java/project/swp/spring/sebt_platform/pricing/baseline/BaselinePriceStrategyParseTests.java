package project.swp.spring.sebt_platform.pricing.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests for parsing depreciation strategy from the `notes` field.
 * We bypass JSON loading by injecting entries directly into the repository map via reflection.
 */
public class BaselinePriceStrategyParseTests {

    private BaselinePriceRepository repository;
    private BaselinePriceService service;

    @BeforeEach
    void setup() throws Exception {
        repository = new BaselinePriceRepository();
        service = new BaselinePriceService(repository);
        // Inject custom data
        Field byBrand = BaselinePriceRepository.class.getDeclaredField("byBrand");
        byBrand.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, List<BaselinePriceEntry>> map = (Map<String, List<BaselinePriceEntry>>) byBrand.get(repository);
        map.clear();
    add("VinFast", entry("VinFast","VF 3","Standard","EV", 299_000_000L, "Mini; linear 10%/year; max 80% depreciation"));
    add("VinFast", entry("VinFast","VF 9","Eco","EV", 1_499_000_000L, "E-SUV; exponential 0.92^age; max 70% depreciation"));
    add("Hyundai", entry("Hyundai","Ioniq 5","Standard","EV", 1_300_000_000L, "Crossover; linear 9%/year"));
    add("BatteryCo", entry("BatteryCo","Pack X","Std","BATTERY", 200_000_000L, "Replacement; linear 6%/year"));
    add("Generic", entry("Generic","Mystery","Std","EV", 100_000_000L, "Just description no pattern"));
    }

    private void add(String brand, BaselinePriceEntry e) throws Exception {
        Field byBrand = BaselinePriceRepository.class.getDeclaredField("byBrand");
        byBrand.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, List<BaselinePriceEntry>> map = (Map<String, List<BaselinePriceEntry>>) byBrand.get(repository);
        map.computeIfAbsent(brand.toLowerCase().trim(), k -> new java.util.ArrayList<>()).add(e);
    }

    private BaselinePriceEntry entry(String brand, String model, String variant, String category, long price, String notes) {
        BaselinePriceEntry e = new BaselinePriceEntry();
        e.setBrand(brand);
        e.setModel(model);
        e.setVariant(variant);
        e.setCategory(category);
        e.setBaselinePrice(price);
        e.setNotes(notes);
        return e;
    }

    @Test
    @DisplayName("Parse linear 10% with max 80% and apply age")
    void testLinearWithMax() {
        BaselinePriceService.LookupResult lr = service.lookup("VinFast","VF 3","Standard", 3);
        assertTrue(lr.found());
        DepreciationStrategy s = lr.strategy();
        assertEquals(DepreciationStrategy.Type.LINEAR, s.getType());
        assertEquals(0.10, s.getRate(), 1e-6);
        assertEquals(0.80, s.getMaxDepreciation(), 1e-6);
        long expected = (long) Math.round(299_000_000L * (1 - 0.10 * 3)); // no cap yet
        assertEquals(expected, lr.depreciatedPrice());
    }

    @Test
    @DisplayName("Parse exponential 0.92 with max 70%")
    void testExponentialWithMax() {
        BaselinePriceService.LookupResult lr = service.lookup("VinFast","VF 9","Eco", 5);
        assertTrue(lr.found());
        DepreciationStrategy s = lr.strategy();
        assertEquals(DepreciationStrategy.Type.EXPONENTIAL, s.getType());
        assertEquals(0.92, s.getRate(), 1e-6);
        assertEquals(0.70, s.getMaxDepreciation(), 1e-6);
        long expected = (long) Math.round(1_499_000_000L * Math.pow(0.92,5));
        assertEquals(expected, lr.depreciatedPrice());
    }

    @Test
    @DisplayName("Parse linear 9%")
    void testLinearNinePercent() {
        BaselinePriceService.LookupResult lr = service.lookup("Hyundai","Ioniq 5","Standard", 2);
        assertTrue(lr.found());
        DepreciationStrategy s = lr.strategy();
        assertEquals(DepreciationStrategy.Type.LINEAR, s.getType());
        assertEquals(0.09, s.getRate(), 1e-6);
        long expected = (long) Math.round(1_300_000_000L * (1 - 0.09 * 2));
        assertEquals(expected, lr.depreciatedPrice());
    }

    @Test
    @DisplayName("Parse linear 6% for battery pack")
    void testLinearBatterySixPercent() {
        BaselinePriceService.LookupResult lr = service.lookup("BatteryCo","Pack X","Std", 4);
        assertTrue(lr.found());
        DepreciationStrategy s = lr.strategy();
        assertEquals(DepreciationStrategy.Type.LINEAR, s.getType());
        assertEquals(0.06, s.getRate(), 1e-6);
        long expected = (long) Math.round(200_000_000L * (1 - 0.06 * 4));
        assertEquals(expected, lr.depreciatedPrice());
    }

    @Test
    @DisplayName("Fallback default linear 7% when no pattern")
    void testFallbackDefaultSevenPercent() {
        BaselinePriceService.LookupResult lr = service.lookup("Generic","Mystery","Std", 1);
        assertTrue(lr.found());
        DepreciationStrategy s = lr.strategy();
        assertEquals(DepreciationStrategy.Type.LINEAR, s.getType());
        assertEquals(0.07, s.getRate(), 1e-6); // default
        long expected = (long) Math.round(100_000_000L * (1 - 0.07));
        assertEquals(expected, lr.depreciatedPrice());
    }
}
