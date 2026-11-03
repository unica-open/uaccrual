/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SecurityProperties securityProperties;

    public JwtAuthenticationFilter() {
        LOG.info("=== JwtAuthenticationFilter istanziato ===");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    	String path = request.getRequestURI();
    	LOG.info("path: " + path);

        // Se l'endpoint è pubblico, imposta autenticazione anonima e prosegui
        if (isPublicEndpoint(request)) {
            LOG.info(">>> Endpoint pubblico: {} - imposta auth anonima <<<", path);
            // Imposta autenticazione anonima per far passare la richiesta
            Authentication anonymousAuth = new UsernamePasswordAuthenticationToken(
                "anonymous", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
            filterChain.doFilter(request, response);
            return;
        }

        // Se l'utente è già autenticato (es. da DevAuthFilter), salto la validazione JWT
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            LOG.debug("Utente già autenticato, salto validazione JWT");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // Token JWT valido → autentica utente
                authenticateJwtUser(jwt);
            } else if (jwt != null) {
                // Token presente ma non valido (LOG già gestito in JwtUtils.validateToken)
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT non valido");
                return;
            } else {
                // Token ASSENTE → richiedi autenticazione
                LOG.warn("Authorization header mancante - richiesta bloccata");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header mancante");
                return;
            }
        } catch (Exception e) {
            LOG.error("Errore durante l'autenticazione JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Errore durante l'autenticazione");
       	 	return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica se il percorso corrente è un endpoint pubblico
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        if (securityProperties.getPublicEndpoints() == null) {
            LOG.warn("publicEndpoints is null!");
            return false;
        }

        // Ottieni il path relativo senza context path
        String relativePath = request.getRequestURI().substring(request.getContextPath().length());
        LOG.info("Checking public endpoint: relativePath='{}', contextPath='{}', uri='{}'", 
                 relativePath, request.getContextPath(), request.getRequestURI());

        for (String publicEndpoint : securityProperties.getPublicEndpoints()) {
            // Matching esatto o con pattern (** per wildcard)
            boolean matches = relativePath.equals(publicEndpoint) || 
                             relativePath.matches(publicEndpoint.replace("**", ".*"));
            LOG.info("  Checking against '{}': matches={}", publicEndpoint, matches);
            if (matches) {
                return true;
            }
        }
        LOG.warn("Endpoint NOT public: {}", relativePath);
        return false;
    }

    public String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        return extractJwtFromBearerToken(bearerToken);
    }

    /**
     * Estrae il JWT da un header Authorization
     * @param authHeader Il valore dell'header Authorization (es: "Bearer eyJhbGciOiJIUzI1NiIs...")
     * @return Il token JWT senza il prefisso "Bearer ", o null se non valido
     */
    public String extractJwtFromBearerToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String jwt = authHeader.substring(BEARER_PREFIX.length());
            LOG.debug("JWT estratto dall'header Authorization");
            return jwt;
        }
        return null;
    }

    /**
     * Autentica un utente con token JWT (produzione)
     */
    private void authenticateJwtUser(String jwt) {
        Claims claims = jwtUtils.parseClaims(jwt);

        // Estrai il codice fiscale dal subject del token
        String codiceFiscale = jwtUtils.getCodiceFiscaleFromToken(jwt);

        // Estrai tutti i dati utente dai claims
        String utenteCodice = jwtUtils.getUtenteCodiceFromToken(jwt);
        String nome = jwtUtils.getNomeFromToken(jwt);
        String cognome = jwtUtils.getCognomeFromToken(jwt);
        // I metodi getEnte* supportano già backward compatibility (nuovi claim snake_case优先)
        String enteCodiceUnivoco = jwtUtils.getEnteCodiceUnivocoFromToken(jwt);
        String enteDenominazione = jwtUtils.getEnteDenominazioneFromToken(jwt);
        String ruolo = jwtUtils.getRuoloFromToken(jwt);
        String source = jwtUtils.getSourceFromToken(jwt);

        // Estrai i ruoli/autorità dal token (se presenti)
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

        // Crea l'oggetto Authentication e impostalo nel SecurityContext
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(codiceFiscale, null, authorities);

        // Aggiungi i claims come dettagli dell'autenticazione
        authentication.setDetails(claims);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOG.info("Utente autenticato con successo (JWT): {} - {} {} {} - Ruolo: {} - Ente: {} {} - Source: {}",
            codiceFiscale, nome, cognome, utenteCodice, ruolo, enteCodiceUnivoco, enteDenominazione, source);
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Estrai il ruolo dal token se presente
        String ruolo = claims.get("ruolo", String.class);
        if (ruolo != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ruolo.toUpperCase()));
        }
        
        // Estrai eventuali permessi/azioni dal token
        @SuppressWarnings("unchecked")
        List<String> permessi = claims.get("permessi", List.class);
        if (permessi != null) {
            for (String permesso : permessi) {
                authorities.add(new SimpleGrantedAuthority(permesso));
            }
        }
        
        // Autorità di default per utenti autenticati
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        return authorities;
    }
}
