/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configurazione Jackson condivisa per tutti i microservizi Accrual.
 *
 * Configura la conversione automatica camelCase Java ↔ snake_case JSON,
 * coerente con le linee guida CSI (contract_first_rest_doc.md):
 * - Java: codiceFiscale, enteCodiceUnivoco, annoBilancio
 * - JSON: codice_fiscale, ente_codice_univoco, anno_bilancio
 *
 * Usata da: accrualsbsrv, accrualauthsbapi, accrualreportsbapi
 */
@Configuration
public class AccrualJacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // snake_case come richiesto dalle linee guida CSI RESTEasy
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // Supporto Java 8 Time (LocalDate, LocalDateTime, ecc.)
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Omette valori nulli dal JSON (payload più leggero)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Backward compatibility: ignora proprietà sconosciute
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
