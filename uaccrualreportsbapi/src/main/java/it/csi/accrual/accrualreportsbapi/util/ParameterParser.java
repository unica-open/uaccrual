/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ParameterParser {

    private final ObjectMapper objectMapper;
    
    // Formati data supportati
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd",      // ISO-8601: 2026-01-01
        "dd/MM/yyyy",      // Europeo: 01/01/2026
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    };

    public ParameterParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parsa una stringa JSON in una Map di parametri
     * Converte automaticamente le stringhe che sembrano date in oggetti Date
     *
     * @param jsonString Stringa JSON con i parametri
     * @return Map di parametri, o una mappa vuota se la stringa è null/vuota
     */
    public Map<String, Object> parseParameters(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return new HashMap<>();
        }

        try {
            log.debug("Parsing parameters: {}", jsonString);

            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(jsonString, Map.class);

            // Convertire le stringhe che sembrano date in oggetti Date
            Map<String, Object> convertedParams = convertDateParameters(params);

            log.debug("Parameters parsed successfully - Count: {}", convertedParams.size());
            return convertedParams;

        } catch (Exception e) {
            log.warn("Failed to parse parameters JSON, returning empty map", e);
            return new HashMap<>();
        }
    }

    /**
     * Converte i parametri stringa che sembrano date in oggetti Date
     */
    private Map<String, Object> convertDateParameters(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return params;
        }

        Map<String, Object> converted = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Se il valore è una stringa e la chiave contiene "data" o "date", prova a convertire
            if (value instanceof String && isDateParameter(key)) {
                Date date = parseDate((String) value);
                if (date != null) {
                    // Usare java.sql.Date per compatibilità con BIRT
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    converted.put(key, sqlDate);
                    log.debug("Converted parameter '{}' to java.sql.Date: {}", key, sqlDate);
                } else {
                    converted.put(key, value);
                    log.warn("Parameter '{}' looks like a date but could not be parsed: {}", key, value);
                }
            }
            // Se il valore è numerico (Long/Integer) e la chiave contiene "data" o "date", converti da timestamp
            else if (value instanceof Number && isDateParameter(key)) {
                long timestamp = ((Number) value).longValue();
                java.sql.Date sqlDate = new java.sql.Date(timestamp);
                converted.put(key, sqlDate);
                log.debug("Converted parameter '{}' from Number timestamp ({}) to java.sql.Date: {}", key, timestamp, sqlDate);
            } else {
                converted.put(key, value);
            }
        }
        return converted;
    }

    /**
     * Verifica se il nome del parametro suggerisce che sia una data
     */
    private boolean isDateParameter(String paramName) {
        String lower = paramName.toLowerCase();
        return lower.contains("data") || lower.contains("date") || 
               lower.startsWith("da_") || lower.startsWith("a_") ||
               lower.endsWith("_da") || lower.endsWith("_a");
    }

    /**
     * Tenta di parsare una stringa come data usando vari formati
     */
    private Date parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(value);
            } catch (ParseException e) {
                // Prova il formato successivo
                log.trace("Failed to parse '{}' with format {}", value, format);
            }
        }
        return null;
    }

    /**
     * Serializza una Map di parametri in una stringa JSON
     *
     * @param params Map di parametri da serializzare
     * @return Stringa JSON, o null se la map è null o vuota
     */
    public String toJson(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(params);
            log.trace("Serialized parameters to JSON: {}", json);
            return json;
        } catch (Exception e) {
            log.warn("Failed to serialize parameters to JSON", e);
            return null;
        }
    }
}
