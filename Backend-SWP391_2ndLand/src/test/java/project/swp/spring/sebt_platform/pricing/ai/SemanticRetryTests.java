package project.swp.spring.sebt_platform.pricing.ai;

import org.junit.jupiter.api.Test;
import project.swp.spring.sebt_platform.pricing.ai.GeminiResponseParser.ParseResult;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests semantic retry behavior via test hook (simulated two responses).
 * First body lacks JSON object, second contains valid JSON.
 */
public class SemanticRetryTests {

    @Test
    void testSecondBodyParses() {
        String first = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"Some explanation without JSON price 12345\" } ] } } ] }";
        String second = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"{\\\"suggestedPrice\\\": 10000000, \\\"reasoning\\\": \\\"ok\\\", \\\"evidence\\\":[\\\"baseline\\\",\\\"capacity\\\"]}\" } ] } } ] }";
        GeminiResponseParser parser = new GeminiResponseParser();
        ParseResult r1 = parser.parse(first);
        // first should fail JSON parse and fallback to number extraction (has 12345)
        assertTrue(r1.numberFallback() || r1.price()==null);
        ParseResult r2 = parser.parse(second);
        assertTrue(r2.jsonParsed());
        assertEquals(10_000_000D, r2.price());
        assertTrue(r2.evidence().contains("baseline") || r2.evidence().contains("capacity"));
    }
}
