/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualcommonsrv.security.JwtProperties;
import jakarta.inject.Inject;

/**
 * Generatore JWT - Responsabile della creazione token per il sistema di autenticazione.
 * Non si occupa di parsing/validation (competenza di JwtUtils in accrualcommonsrv).
 */
@Component("jwtGenerator")
public class JwtGenerator {

	@Inject private JwtProperties jwtProperties;

    public String generateJwtString(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio) {
        return Jwts.builder()
                .setIssuer("accrualauth")
                .setSubject(user.getUtenteCodiceUnivoco())
                .setAudience("accrual-services")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .setId(UUID.randomUUID().toString())
                .addClaims(createClaims(user, provider, annoBilancio, faseBilancio))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Map<String, Object> createClaims(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("nonce", UUID.randomUUID().toString());
        claims.put("utente_codice", user.getUtenteCodice());
        claims.put("nome", user.getUtenteDescrizione());
        claims.put("cognome", user.getUtenteDescrizione());
        claims.put("codice_ente", user.getEnte().getEnteCodiceUnivoco());
        claims.put("denominazione_ente", user.getEnte().getEnteDenominazione());
        claims.put("anno_bilancio", annoBilancio);
        claims.put("fase_bilancio", faseBilancio);
        claims.put("ruolo", user.getRuolo().getRuoloCodice());
        claims.put("source", provider);
        return claims;
    }
}
