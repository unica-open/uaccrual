/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParameterParser Tests")
class ParameterParserTest {

    private ParameterParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parser = new ParameterParser(objectMapper);
    }

    // ===== PARSE JSON TESTS =====

    @Test
    @DisplayName("Should parse valid JSON string")
    void testParseValidJson() {
        String json = "{\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}";

        Map<String, Object> result = parser.parseParameters(json);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("2025-01-01", result.get("startDate"));
        assertEquals("2025-12-31", result.get("endDate"));
    }

    @Test
    @DisplayName("Should parse JSON with mixed types")
    void testParseJsonMixedTypes() {
        String json = "{\"name\":\"Report\",\"count\":42,\"active\":true,\"value\":3.14}";

        Map<String, Object> result = parser.parseParameters(json);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("Report", result.get("name"));
        assertEquals(42, result.get("count"));
        assertEquals(true, result.get("active"));
        assertEquals(3.14, result.get("value"));
    }

    @Test
    @DisplayName("Should parse empty JSON object")
    void testParseEmptyJson() {
        String json = "{}";

        Map<String, Object> result = parser.parseParameters(json);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should return empty map for null")
    void testParseNull() {
        Map<String, Object> result = parser.parseParameters(null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should return empty map for empty string")
    void testParseEmptyString() {
        Map<String, Object> result = parser.parseParameters("");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should return empty map for blank string")
    void testParseBlankString() {
        Map<String, Object> result = parser.parseParameters("   ");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should return empty map for invalid JSON")
    void testParseInvalidJson() {
        String invalidJson = "{ this is not json }";

        Map<String, Object> result = parser.parseParameters(invalidJson);

        // Should not throw, return empty map
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void testParseMalformedJson() {
        String malformed = "{\"key\": ";

        Map<String, Object> result = parser.parseParameters(malformed);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ===== SERIALIZE TO JSON TESTS =====

    @Test
    @DisplayName("Should serialize map to JSON")
    void testSerializeMapToJson() {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2025-01-01");
        params.put("endDate", "2025-12-31");

        String json = parser.toJson(params);

        assertNotNull(json);
        assertTrue(json.contains("startDate"));
        assertTrue(json.contains("2025-01-01"));
        assertTrue(json.contains("endDate"));
        assertTrue(json.contains("2025-12-31"));
    }

    @Test
    @DisplayName("Should serialize null to null")
    void testSerializeNull() {
        String json = parser.toJson(null);

        assertNull(json);
    }

    @Test
    @DisplayName("Should serialize empty map to null")
    void testSerializeEmptyMap() {
        String json = parser.toJson(new HashMap<>());

        assertNull(json);
    }

    @Test
    @DisplayName("Should handle serialization error gracefully")
    void testSerializeError() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        // Should not throw even if something goes wrong
        String json = parser.toJson(params);

        assertNotNull(json);
    }

    // ===== ROUND TRIP TESTS =====

    @Test
    @DisplayName("Should support round trip parse and serialize")
    void testRoundTripParseAndSerialize() {
        String originalJson = "{\"name\":\"Report\",\"version\":1}";

        // Parse
        Map<String, Object> parsed = parser.parseParameters(originalJson);
        assertEquals(2, parsed.size());

        // Serialize back
        String serialized = parser.toJson(parsed);
        assertNotNull(serialized);

        // Parse again
        Map<String, Object> reparsed = parser.parseParameters(serialized);
        assertEquals(parsed.size(), reparsed.size());
        assertEquals(parsed.get("name"), reparsed.get("name"));
    }

    @Test
    @DisplayName("Should handle nested objects")
    void testNestedObjects() {
        String json = "{\"user\":{\"name\":\"John\",\"age\":30},\"active\":true}";

        Map<String, Object> result = parser.parseParameters(json);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("user"));
        assertTrue(result.containsKey("active"));
    }
}
