/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.be.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro JAX-RS per sanificare il corpo della richiesta prima della deserializzazione.
 * Questo filtro legge l'intero stream di input, lo corregge se necessario,
 * e lo sostituisce nel contesto della richiesta, permettendo ai componenti
 * successivi (es. Jackson) di leggerlo correttamente.
 */
@Component
@Provider
@Priority(100) // Eseguito con alta priorità, prima di altri filtri come EnteContextFilter
public class BodySanitizerFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BodySanitizerFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getPath();
        String method = requestContext.getMethod();

        // Applica il filtro solo all'endpoint specifico
        if (path.endsWith("/report/generate") && "POST".equalsIgnoreCase(method)) {

            LOG.info("BodySanitizerFilter: Intercettata richiesta POST per /report/generate");

            // Legge il corpo della richiesta (consumando lo stream originale)
            String originalBody = readInputStream(requestContext.getEntityStream());
            LOG.debug("BodySanitizerFilter: Corpo originale ricevuto: '{}'", originalBody);

            if (originalBody == null || originalBody.isEmpty()) {
                LOG.warn("BodySanitizerFilter: Il corpo della richiesta è vuoto o nullo.");
                // Non si esce qui, si prosegue per sostituire lo stream consumato
            }

            // Sanifica il corpo
            String sanitizedBody = sanitizeBody(originalBody);

            if (!originalBody.equals(sanitizedBody)) {
                LOG.info("BodySanitizerFilter: Corpo sanificato: '{}'", sanitizedBody);
            } else {
                LOG.debug("BodySanitizerFilter: Il corpo non necessitava di sanificazione.");
            }

            // Sostituisce sempre lo stream nel contesto della richiesta, perché quello originale è stato consumato
            InputStream sanitizedInputStream = new ByteArrayInputStream(sanitizedBody.getBytes(StandardCharsets.UTF_8));
            requestContext.setEntityStream(sanitizedInputStream);
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private String sanitizeBody(String body) {
        if (body == null || body.isEmpty()) {
            return ""; // Restituisce stringa vuota per creare un InputStream valido
        }

        // La logica di trim() è spesso sufficiente per rimuovere whitespace e caratteri di controllo iniziali/finali
        String trimmedBody = body.trim();

        // Rimuove un eventuale Byte Order Mark (BOM) UTF-8 che a volte viene introdotto
        if (trimmedBody.startsWith("\uFEFF")) {
        	LOG.warn("BodySanitizerFilter: Rimosso BOM (Byte Order Mark) UTF-8 dall'inizio del corpo.");
            return trimmedBody.substring(1);
        }

        // Se dopo il trim, il corpo non inizia con '{', potrebbe esserci un carattere non valido.
        if (!trimmedBody.startsWith("{")) {
            int firstBraceIndex = trimmedBody.indexOf('{');
            if (firstBraceIndex > 0) {
                String prefix = trimmedBody.substring(0, firstBraceIndex);
                LOG.warn("BodySanitizerFilter: Rimosso prefisso non JSON non valido dall'inizio del corpo: '{}'", prefix);
                return trimmedBody.substring(firstBraceIndex);
            }
        }

        return trimmedBody;
    }
}