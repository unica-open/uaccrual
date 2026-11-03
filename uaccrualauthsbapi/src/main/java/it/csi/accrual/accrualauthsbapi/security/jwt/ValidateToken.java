/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.security.jwt;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTTokenRepository;
import it.csi.accrual.accrualcommonsrv.security.JwtUtils;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@Component
public class ValidateToken {

    @Inject	private AccrualAuthTTokenRepository tokenRepository;
    @Inject private TokenService tokenService;
    @Inject	private JwtUtils jwtUtils;

    public Claims validateToken(String token) {
        Claims claims = jwtUtils.parseClaims(token);

        if (claims.getExpiration().before(new Date())) {
            tokenService.findByTokenNonce(claims.get("nonce").toString())
                    .ifPresentOrElse(t -> {
                        t.setFineValidita(LocalDateTime.now());
                        t.setGestDataDel(LocalDateTime.now());
                        tokenRepository.saveAndFlush(t);
                    }, () -> {
                    	
                        throw new BadRequestException("Token non trovato");
                    });
            throw new BadRequestException("Token non valido");
        }
        return claims;
    }

    
}