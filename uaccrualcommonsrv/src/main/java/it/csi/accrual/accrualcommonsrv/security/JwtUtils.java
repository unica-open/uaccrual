/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;


import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	private final JwtProperties jwtProperties;
	private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);

    public JwtUtils(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

    /** Genera la chiave segreta HMAC da stringa in chiaro */
    public SecretKey getSigningKey() {
    	String secret = jwtProperties.getSecret();
    	LOG.warn("DEBUG - JwtUtils (Validazione) - JWT Secret attuale (in chiaro): {}", secret);
    	byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            LOG.warn("Token JWT scaduto: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            LOG.warn("Token JWT malformato: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            LOG.warn("Firma del token JWT non valida: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            LOG.warn("Token JWT non supportato: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.warn("Errore generico validazione token JWT: {}", e.getMessage());
            return false;
        }
    }

    public String getCodiceFiscaleFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== Metodi per estrarre i claims personalizzati ====================

    /**
     * Estrae il codice utente (utente_codice) dal token
     */
    public String getUtenteCodiceFromToken(String token) {
        return parseClaims(token).get("utente_codice", String.class);
    }

    /**
     * Estrae il nome utente dal token
     */
    public String getNomeFromToken(String token) {
        return parseClaims(token).get("nome", String.class);
    }

    /**
     * Estrae il cognome utente dal token
     */
    public String getCognomeFromToken(String token) {
        return parseClaims(token).get("cognome", String.class);
    }

    /**
     * Estrae il codice univoco dell'ente dal token.
     * Legge il claim 'codice_ente' dal JWT.
     */
    public String getEnteCodiceUnivocoFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("codice_ente", String.class);
    }

    /**
     * Estrae la denominazione dell'ente dal token.
     * Legge il claim 'denominazione_ente' dal JWT.
     */
    public String getEnteDenominazioneFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("denominazione_ente", String.class);
    }

    /**
     * Estrae il ruolo dell'utente dal token
     */
    public String getRuoloFromToken(String token) {
        return parseClaims(token).get("ruolo", String.class);
    }

    /**
     * Estrae il provider di autenticazione dal token
     */
    public String getSourceFromToken(String token) {
        return parseClaims(token).get("source", String.class);
    }

    /**
     * Estrae il nonce dal token (identificativo univoco del token)
     */
    public String getNonceFromToken(String token) {
        return parseClaims(token).get("nonce", String.class);
    }

    public Integer getAnnoEsercizioFromToken(String token) {
        return parseClaims(token).get("esercizio", Integer.class);
    }

    /**
     * Estrae la fase di bilancio dal token.
     * Supporta sia il nuovo claim snake_case (fase_bilancio) che il vecchio (faseBilancio).
     */
    public String getFaseBilancioFromToken(String token) {
        Claims claims = parseClaims(token);
        // Prima cerca il nuovo claim snake_case, poi il vecchio per backward compatibility
        String faseBilancio = claims.get("fase_bilancio", String.class);
        if (faseBilancio == null) {
            faseBilancio = claims.get("faseBilancio", String.class);
        }
        return faseBilancio;
    }

    /**
     * Estrae tutti i claims come mappa
     */
    public Map<String, Object> getAllClaimsFromToken(String token) {
        return parseClaims(token);
    }
}
