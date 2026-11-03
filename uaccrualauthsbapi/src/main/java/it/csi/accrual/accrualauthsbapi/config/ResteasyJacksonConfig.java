/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

/**
 * Configurazione Jackson per RESTEasy (JAX-RS).
 * Configura snake_case per serializzazione/deserializzazione JSON.
 */
@Provider
@Component
@Produces(MediaType.APPLICATION_JSON)
public class ResteasyJacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public ResteasyJacksonConfig() {
        objectMapper = new ObjectMapper();
        // Conversione automatica camelCase Java <-> snake_case JSON
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // Supporto Java 8 Time
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Omette valori nulli
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Ignora propriet sconosciute
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
