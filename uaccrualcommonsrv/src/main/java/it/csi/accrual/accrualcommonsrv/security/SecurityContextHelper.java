/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;

import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper per estrarre informazioni di sicurezza dal SecurityContext.
 *
 * Fornisce metodi per recuperare i claim JWT dall'autenticazione corrente
 * senza dover passare header HTTP espliciti.
 */
@Component
public class SecurityContextHelper {

    /**
     * Estrae il codice univoco dell'ente dal token JWT corrente (metodo statico).
     * Legge il claim 'codice_ente' dal JWT.
     *
     * @return Il codice univoco dell'ente, o null se non disponibile
     */
    public static String getEnteCodiceUnivocoStatic() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        // I claim JWT sono salvati nei details dell'autenticazione
        // Supporta sia Claims (JWT reale) che Map (token dev)
        if (authentication.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) authentication.getDetails();
            return claims.get("codice_ente", String.class);
        } else if (authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) authentication.getDetails();
            return (String) detailsMap.get("codice_ente");
        }

        return null;
    }

    /**
     * Estrae il codice univoco dell'ente dal token JWT corrente.
     * Legge il claim 'codice_ente' dal JWT.
     *
     * @return Il codice univoco dell'ente, o null se non disponibile
     */
    public String getEnteCodiceUnivoco() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        // I claim JWT sono salvati nei details dell'autenticazione
        // Supporta sia Claims (JWT reale) che Map (token dev)
        if (authentication.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) authentication.getDetails();
            return claims.get("codice_ente", String.class);
        } else if (authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) authentication.getDetails();
            return (String) detailsMap.get("codice_ente");
        }

        return null;
    }

    /**
     * Estrae la denominazione dell'ente dal token JWT corrente.
     * Legge il claim 'denominazione_ente' dal JWT.
     *
     * @return La denominazione dell'ente, o null se non disponibile
     */
    public String getEnteDenominazione() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        // Supporta sia Claims (JWT reale) che Map (token dev)
        if (authentication.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) authentication.getDetails();
            return claims.get("denominazione_ente", String.class);
        } else if (authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) authentication.getDetails();
            return (String) detailsMap.get("denominazione_ente");
        }

        return null;
    }

    /**
     * Estrae il claim 'codiceFiscale' (subject) dal token JWT corrente.
     *
     * @return Il codice fiscale dell'utente, o null se non disponibile
     */
    public String getCodiceFiscale() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Estrae il claim 'ruolo' dal token JWT corrente.
     *
     * @return Il ruolo dell'utente, o null se non disponibile
     */
    public String getRuolo() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        // Supporta sia Claims (JWT reale) che Map (token dev)
        if (authentication.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) authentication.getDetails();
            return claims.get("ruolo", String.class);
        } else if (authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) authentication.getDetails();
            return (String) detailsMap.get("ruolo");
        }

        return null;
    }

    /**
     * Estrae l'anno di bilancio dal token JWT corrente (metodo statico).
     * Legge il claim 'anno_bilancio' (snake_case).
     *
     * @return L'anno di bilancio come Integer, o null se non disponibile
     */
    public static Integer getAnnoBilancioStatic() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        if (authentication.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) authentication.getDetails();
            return claims.get("anno_bilancio", Integer.class);
        } else if (authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) authentication.getDetails();
            // Il frontend dev invia come numero, ma gestiamo anche String
            Object annoObj = detailsMap.get("anno_bilancio");
            if (annoObj instanceof Number) {
                return ((Number) annoObj).intValue();
            } else if (annoObj instanceof String) {
                try {
                    return Integer.parseInt((String) annoObj);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Estrae l'anno di bilancio dal token JWT corrente.
     * Legge il claim 'anno_bilancio' (snake_case).
     *
     * @return L'anno di bilancio come Integer, o null se non disponibile
     */
    public Integer getAnnoBilancio() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        if (authentication.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) authentication.getDetails();
            return claims.get("anno_bilancio", Integer.class);
        } else if (authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) authentication.getDetails();
            // Il frontend dev invia come numero, ma gestiamo anche String
            Object annoObj = detailsMap.get("anno_bilancio");
            if (annoObj instanceof Number) {
                return ((Number) annoObj).intValue();
            } else if (annoObj instanceof String) {
                try {
                    return Integer.parseInt((String) annoObj);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Verifica se l'utente corrente è autenticato.
     *
     * @return true se l'utente è autenticato, false altrimenti
     */
    public boolean isAuthenticated() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
