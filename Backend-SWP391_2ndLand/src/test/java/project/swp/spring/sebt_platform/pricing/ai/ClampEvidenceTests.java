package project.swp.spring.sebt_platform.pricing.ai;

import org.junit.jupiter.api.Test;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;
import project.swp.spring.sebt_platform.service.impl.PricingServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that when AI raw price exceeds range, clamp evidence is added.
 * We simulate by invoking private enrichedFinalFromAi via reflection.
 */
public class ClampEvidenceTests {

    @Test
    void testClampAddsEvidence() throws Exception {
        // Instantiate service (null dependency ok for this path)
        PricingServiceImpl svc = new PricingServiceImpl(null);

        // Access inner HeuristicResult class
        Class<?>[] inner = PricingServiceImpl.class.getDeclaredClasses();
        Class<?> heurClass = null;
        for (Class<?> c : inner) if (c.getSimpleName().equals("HeuristicResult")) heurClass = c;
        assertNotNull(heurClass, "HeuristicResult inner class not found");
        Constructor<?> ctor = heurClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object hr = ctor.newInstance();
        // Set baseline & factorSummary
        Field baselineF = heurClass.getDeclaredField("baseline"); baselineF.setAccessible(true); baselineF.setLong(hr, 10_000_000L);
        Field factorSummaryF = heurClass.getDeclaredField("factorSummary"); factorSummaryF.setAccessible(true); factorSummaryF.set(hr, "age=1.0,cap=1.0,cond=1.0,km=1.0,health=1.0");

        // Invoke enrichedFinalFromAi(aiRawPriceAboveMax,...)
        Method enriched = PricingServiceImpl.class.getDeclaredMethod("enrichedFinalFromAi", Double.class, String.class, String.class, Long.class, Long.class, Long.class, String.class, String.class, heurClass, double.class);
        enriched.setAccessible(true);
        PricingSuggestResponseDTO dto = (PricingSuggestResponseDTO) enriched.invoke(svc, 15_000_000D, "test", "model", 10_000_000L, 9_000_000L, 11_000_000L, "vTest", "prompt", hr, 0.1d);
        assertTrue(dto.getClamped(), "Should be clamped");
        assertNotNull(dto.getEvidence());
        assertTrue(dto.getEvidence().contains("clamp"), "Evidence should contain clamp tag");
    }
}
