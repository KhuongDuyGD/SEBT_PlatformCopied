package project.swp.spring.sebt_platform.pricing.ai;

import org.junit.jupiter.api.Test;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;
import project.swp.spring.sebt_platform.service.impl.PricingServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/** Ensure final suggested price never exceeds baseline (new vehicle price). */
public class BaselineCapTests {

    @Test
    void testPriceCappedAtBaseline() throws Exception {
        PricingServiceImpl svc = new PricingServiceImpl(null);
        // Access inner HeuristicResult
        Class<?> heurClass = null;
        for (Class<?> c : PricingServiceImpl.class.getDeclaredClasses()) if (c.getSimpleName().equals("HeuristicResult")) heurClass = c;
        assertNotNull(heurClass);
        Constructor<?> ctor = heurClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object hr = ctor.newInstance();
        Field baselineF = heurClass.getDeclaredField("baseline"); baselineF.setAccessible(true); baselineF.setLong(hr, 1_019_000_000L);
        Field factorSummaryF = heurClass.getDeclaredField("factorSummary"); factorSummaryF.setAccessible(true); factorSummaryF.set(hr, "age=1.0,cap=1.0,cond=1.0,km=1.0,health=1.0");
        Method enriched = PricingServiceImpl.class.getDeclaredMethod("enrichedFinalFromAi", Double.class, String.class, String.class, Long.class, Long.class, Long.class, String.class, String.class, heurClass, double.class);
        enriched.setAccessible(true);
        // AI raw price intentionally above baseline
        PricingSuggestResponseDTO dto = (PricingSuggestResponseDTO) enriched.invoke(svc, 1_330_035_000D, "test", "model", 1_177_022_000L, 1_024_009_000L, 1_330_035_000L, "vTest", "prompt", hr, 0.13d);
        assertNotNull(dto.getSuggestedPrice());
    assertTrue(dto.getSuggestedPrice() <= 1_019_000_000L, "Final price should be capped at baseline");
    assertTrue(dto.getClamped(), "Should mark clamped when baseline cap applied");
    assertNotNull(dto.getEvidence());
    assertTrue(dto.getEvidence().contains("baseline-cap"), "Evidence should contain baseline-cap tag");
    }
}
