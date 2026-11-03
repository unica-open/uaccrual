/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro di autenticazione per ambiente di sviluppo.
 * 
 * Gestisce i token dev (JSON codificato in base64 semplice) 
 * inviati dal frontend durante lo sviluppo locale.
 * 
 * ATTIVO SOLO SE dev.auth.enabled=true
 * 
 * Configurazione:
 * - application-local.properties: dev.auth.enabled=true
 * - application-docker.properties: dev.auth.enabled=false (o non impostato)
 * 
 * @see JwtAuthenticationFilter (produzione - token JWT)
 */
@Component
@ConditionalOnProperty(name = "dev.auth.enabled", havingValue = "true")
@Order(1)  // Esegue PRIMA di JwtAuthenticationFilter
public class DevAuthFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DevAuthFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public DevAuthFilter() {
        LOG.info("=== DevAuthFilter istanziato (dev.auth.enabled=true) ===");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        LOG.debug("DevAuthFilter - processing request: {}", request.getRequestURI());
        
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            LOG.debug("DevAuthFilter - token ricevuto: {}...", token.substring(0, Math.min(20, token.length())));

            // Verifica se è un token dev
            if (isDevToken(token)) {
                LOG.info("DevAuthFilter - token dev riconosciuto");
                try {
                    authenticateDevUser(token);
                    LOG.info("Utente autenticato con token dev");
                    // Token dev autenticato con successo, passa direttamente alla chain
                    // SENZA far processare il token da JwtAuthenticationFilter
                    filterChain.doFilter(request, response);
                    return;
                } catch (Exception e) {
                    LOG.warn("Token dev non valido: {}", e.getMessage());
                    // Non bloccare la richiesta, lascia passare al JwtAuthenticationFilter
                }
            }
            // Se non è un token dev, lascia gestire al JwtAuthenticationFilter
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Verifica se il token è un token dev (base64 semplice o JWT con alg: none)
     *
     * Strategia:
     * 1. Prova a decodificare come base64 semplice → se funziona, è token dev
     * 2. Se fallisce, prova come JWT → se alg: none, è token dev
     *
     * Un token dev può essere:
     * 1. Base64 semplice di un JSON (payload diretto)
     * 2. JWT con alg: none (header.payload con signature vuota)
     */
    private boolean isDevToken(String token) {
        // Prova 1: è base64 semplice di un JSON?
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(decoded);
            
            if (json.isObject() && (json.has("codiceFiscale") || json.has("sub"))) {
                LOG.debug("Token dev rilevato: base64 semplice");
                return true;
            }
        } catch (Exception e) {
            // Non è base64 semplice, prova come JWT
        }
        
        // Prova 2: è un JWT con alg: none?
        try {
            String[] parts = token.split("\\.", 3);
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode header = mapper.readTree(headerJson);
            String alg = header.has("alg") ? header.get("alg").asText() : "";
            
            if ("none".equals(alg)) {
                LOG.debug("Token dev rilevato: JWT con alg: none");
                return true;
            }
        } catch (Exception e) {
            // Non è un JWT valido
            LOG.debug("Token non è un token dev valido");
        }
        
        return false;
    }

    /**
     * Autentica un utente con token dev (base64 semplice o JWT con alg: none)
     *
     * Il token può essere:
     * 1. Base64 semplice di un JSON (formato legacy)
     * 2. JWT con alg: none (header.payload.signature vuota)
     *
     * Payload atteso:
     * {
     *   codiceFiscale: string,
     *   nome: string,
     *   cognome: string,
     *   enteCodiceUnivoco: string,
     *   enteDenominazione: string,
     *   ruolo: string,
     *   source: 'DEV'
     * }
     */
    private void authenticateDevUser(String token) throws IOException {
        String decoded;

        // Verifica se è un JWT con alg: none (contiene punti)
        if (token.contains(".")) {
            // Estrai il payload (seconda parte)
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                // Decodifica il payload (base64 url-safe)
                decoded = new String(Base64.getUrlDecoder().decode(parts[1]));
            } else {
                throw new IOException("Token JWT malformato");
            }
        } else {
            // Token base64 semplice (legacy)
            decoded = new String(Base64.getDecoder().decode(token));
        }

        // Parse JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(decoded);

        // Estrai i dati utente (supporta sia snake_case che camelCase per backward compatibility)
        String codiceFiscale = json.has("sub") ? json.get("sub").asText() :
                               (json.has("codiceFiscale") ? json.get("codiceFiscale").asText() : null);
        String nome = json.has("nome") ? json.get("nome").asText() : null;
        String cognome = json.has("cognome") ? json.get("cognome").asText() : null;
        // Legge i claim snake_case dal token dev
        String enteCodiceUnivoco = json.has("codice_ente") ? json.get("codice_ente").asText() : null;
        String enteDenominazione = json.has("denominazione_ente") ? json.get("denominazione_ente").asText() : null;
        String ruolo = json.has("ruolo") ? json.get("ruolo").asText() : null;
        String source = json.has("source") ? json.get("source").asText() : null;
        // Estrai anno_bilancio (snake_case). Se non presente, sarà null.
        Integer annoBilancio = null;
        if (json.has("anno_bilancio")) {
            annoBilancio = json.get("anno_bilancio").asInt();
            LOG.debug("DevAuthFilter - anno_bilancio estratto dal token (snake_case): {}", annoBilancio);
        }

        // Crea authorities
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + ruolo.toUpperCase()));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Crea authentication
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(codiceFiscale, null, authorities);

        // Crea un oggetto Claims simulato con i dati del token dev
        // Questo permette a SecurityContextHelper di leggere i claim come se fosse un JWT reale
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", codiceFiscale);
        claims.put("codice_fiscale", codiceFiscale);
        claims.put("nome", nome);
        claims.put("cognome", cognome);
        // Claim snake_case per SecurityContextHelper
        claims.put("codice_ente", enteCodiceUnivoco);
        claims.put("denominazione_ente", enteDenominazione);
        claims.put("anno_bilancio", annoBilancio);
        claims.put("ruolo", ruolo);
        claims.put("source", source);

        // Imposta i claims come details dell'autenticazione
        authentication.setDetails(claims);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOG.info("DevAuthFilter - Utente autenticato (DEV): {} - {} {} - Ruolo: {} - Ente: {} {} - Anno Bilancio: {}",
            codiceFiscale, nome, cognome, ruolo, enteCodiceUnivoco, enteDenominazione, annoBilancio);
    }
}
