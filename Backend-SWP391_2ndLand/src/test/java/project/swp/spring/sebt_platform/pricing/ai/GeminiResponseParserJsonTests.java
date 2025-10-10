package project.swp.spring.sebt_platform.pricing.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Additional tests for GeminiResponseParser JSON extraction scenarios */
public class GeminiResponseParserJsonTests {

    @Test
    void testMultipleCandidatesTakesAllText() {
        String body = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"noise\" }, { \"text\": \"{\\\"suggestedPrice\\\": 7777000, \\\"reasoning\\\": \\\"ok\\\", \\\"evidence\\\":[\\\"baseline\\\",\\\"capacity\\\"]}\" } ] } }, { \"content\": { \"parts\": [ { \"text\": \"ignored extra\" } ] } } ] }";
        GeminiResponseParser parser = new GeminiResponseParser();
        var res = parser.parse(body);
        assertEquals(7777000.0, res.price());
        assertEquals(2, res.evidence().size());
    }

    @Test
    void testFallbackNumberExtraction() {
        String body = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"Estimated price maybe around 8 900 000 VND thanks\" } ] } } ] }";
        GeminiResponseParser parser = new GeminiResponseParser();
        var res = parser.parse(body);
        assertTrue(res.numberFallback());
        assertNotNull(res.price());
        assertTrue(res.price() > 0);
        assertTrue(res.evidence().isEmpty());
    }
}
