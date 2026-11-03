/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.csi.accrual.accrualsbsrv.be.JacksonConfig;
import it.csi.accrual.accrualsbsrv.dto.PdcFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class PdcFilterMessageBodyReader implements MessageBodyReader<PdcFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(PdcFilterMessageBodyReader.class);
    
    private final ObjectMapper objectMapper;

    public PdcFilterMessageBodyReader() {
        // Creiamo una nuova istanza di ObjectMapper con la stessa configurazione di JacksonConfig
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        LOG.debug("isReadable called for type: {}, mediaType: {}", type, mediaType);
        return type == PdcFilter.class && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public PdcFilter readFrom(Class<PdcFilter> type, Type genericType, Annotation[] annotations, 
                             MediaType mediaType, MultivaluedMap<String, String> httpHeaders, 
                             InputStream entityStream) throws IOException, WebApplicationException {
        
        LOG.info("Inizio lettura corpo della richiesta per PdcFilter");
        
        try {
            // Leggiamo l'intero stream per vedere cosa contiene
            byte[] bodyBytes = entityStream.readAllBytes();
            String bodyString = new String(bodyBytes);
            
            LOG.info("Corpo della richiesta ricevuto: '{}'", bodyString);
            LOG.info("Lunghezza corpo: {}, Primi 10 caratteri: '{}'", bodyString.length(), 
                     bodyString.length() > 10 ? bodyString.substring(0, 10) : bodyString);
            
            // Controlliamo se il corpo inizia con un carattere strano
            if (!bodyString.trim().startsWith("{") && !bodyString.trim().isEmpty()) {
                LOG.error("Il corpo della richiesta non inizia con '{{', potrebbe essere corrotto: '{}'", bodyString.charAt(0));
            }
            
            // Se il corpo è vuoto o contiene solo spazi, restituiamo un oggetto PdcFilter vuoto
            if (bodyString.trim().isEmpty()) {
                LOG.warn("Corpo della richiesta vuoto, restituisco PdcFilter vuoto");
                return new PdcFilter();
            }
            
            // Deserializziamo il corpo usando ObjectMapper
            PdcFilter filter = objectMapper.readValue(bodyString, PdcFilter.class);
            
            LOG.info("PdcFilter deserializzato con successo: {}", filter != null ? "presente" : "nullo");
            if (filter != null) {
                LOG.debug("Valori del filtro: famiglia={}, codiceEvento={}, pdcClasseCodice={}", 
                         filter.getFamiglia(), filter.getCodiceEvento(), filter.getPdcClasseCodice());
            }
            
            return filter;
            
        } catch (JsonProcessingException e) {
            LOG.error("Errore durante il parsing JSON del corpo della richiesta: {}", e.getMessage(), e);
            LOG.error("Dettagli corpo della richiesta che ha causato l'errore:");
            
            // Riproviamo a leggere il corpo per loggarlo (dobbiamo ricrearlo perché è stato consumato)
            byte[] bodyBytes = entityStream.readAllBytes();
            String bodyString = new String(bodyBytes);
            LOG.error("Corpo della richiesta: '{}'", bodyString);
            
            throw new WebApplicationException("Errore durante l'elaborazione del corpo della richiesta JSON: " + e.getMessage(), e, 400);
        } catch (Exception e) {
            LOG.error("Errore generico durante la lettura del corpo della richiesta: {}", e.getMessage(), e);
            throw new WebApplicationException("Errore durante la lettura del corpo della richiesta: " + e.getMessage(), e, 400);
        }
    }
}