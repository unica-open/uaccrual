/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import it.csi.accrual.accrualsbsrv.dto.Base;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Customizzazioni specifiche di accrualsbsrv per ObjectMapper.
 * 
 * La configurazione base snake_case è fornita da AccrualJacksonConfig (commonsrv).
 * Qui si aggiungono solo le customizzazioni specifiche di questo modulo.
 */
@Configuration
public class JsonConfig {

    public static class FlexibleLongDeserializer extends JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = jsonParser.getValueAsString();
            if (value == null || value.isEmpty()) {
                return null;
            }

            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                try {
                    Double doubleValue = Double.valueOf(value);
                    if (doubleValue.equals(Math.floor(doubleValue))) {
                        return doubleValue.longValue();
                    } else {
                        throw new NumberFormatException("Il valore non è un numero intero valido: " + value);
                    }
                } catch (NumberFormatException ex) {
                    throw new NumberFormatException("Impossibile convertire il valore a Long: " + value);
                }
            }
        }
    }

    /**
     * Customizza l'ObjectMapper comune con le specifiche di accrualsbsrv:
     * - FlexibleLongDeserializer per gestire stringhe numeriche
     * - BaseMixin per la classe Base
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer accrualsbsrvObjectMapperCustomizer() {
        return builder -> builder.postConfigurer(mapper -> {
            // Mixin per la classe Base
            mapper.addMixIn(Base.class, BaseMixin.class);

            // Deserializer flessibile per Long
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Long.class, new FlexibleLongDeserializer());
            module.addDeserializer(Long.TYPE, new FlexibleLongDeserializer());
            mapper.registerModule(module);
        });
    }
}
