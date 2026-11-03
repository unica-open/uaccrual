/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.security.jwt;

import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;

@Component
public class OpenIdValidator {

    @Inject private ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> extractClaims(String jwtToken) throws Exception {
        String[] parts = jwtToken.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Token non valido");

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        return mapper.readValue(payloadJson, Map.class);
    }

    public String extractEmail(String jwtToken) throws Exception {
        return (String) extractClaims(jwtToken).get("email");
    }
}
