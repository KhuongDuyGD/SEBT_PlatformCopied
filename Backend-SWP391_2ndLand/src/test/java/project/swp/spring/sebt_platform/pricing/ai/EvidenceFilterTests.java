package project.swp.spring.sebt_platform.pricing.ai;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for internal evidence tag filtering logic.
 * We use reflection to invoke private method validateAndFilterEvidence.
 */
public class EvidenceFilterTests {

    @Test
    void testValidAndDuplicateAndSynonym() {
        GeminiResponseParser parser = new GeminiResponseParser();
        String body = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"{\\\"suggestedPrice\\\": 12345678, \\\"reasoning\\\": \\\"r\\\", \\\"evidence\\\":[\\\"baseline\\\",\\\"age\\\",\\\"capacity\\\",\\\"invalid_tag\\\",\\\"capacity\\\",\\\"health\\\"]}\" } ] } } ] }";
        var res = parser.parse(body);
        assertEquals(12345678.0, res.price());
        assertEquals(List.of("baseline","depreciation","capacity","health"), res.evidence());
    }

    @Test
    void testNoEvidenceArray() {
        GeminiResponseParser parser = new GeminiResponseParser();
        String body = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"{\\\"suggestedPrice\\\": 5550000, \\\"reasoning\\\": \\\"x\\\"}\" } ] } } ] }";
        var res = parser.parse(body);
        assertEquals(5550000.0, res.price());
        assertTrue(res.evidence().isEmpty());
    }

    @Test
    void testOnlyInvalid() {
        GeminiResponseParser parser = new GeminiResponseParser();
        String body = "{\n  \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"{\\\"suggestedPrice\\\": 9990000, \\\"reasoning\\\": \\\"x\\\", \\\"evidence\\\":[\\\"xxx\\\",\\\"yyy\\\"]}\" } ] } } ] }";
        var res = parser.parse(body);
        assertEquals(9990000.0, res.price());
        assertTrue(res.evidence().isEmpty());
    }
}
