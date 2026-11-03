/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTToken;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTTokenRepository;
import it.csi.accrual.accrualauthsbapi.security.jwt.JwtGenerator;
import it.csi.accrual.accrualcommonsrv.security.JwtUtils;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

	@Inject
	AccrualAuthTTokenRepository accrualAuthTTokenRepository;
	@Inject
	JwtGenerator jwtGenerator;
	@Inject
	JwtUtils jwtUtils;


	@Override
	public String generateOrReplaceToken(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio) {
		log.info("[Shibboleth-SSO] TokenService: Invalidazione token esistenti per utente ID: {}", user.getUtenteId());
		
		List<AccrualAuthTToken> existingTokens = accrualAuthTTokenRepository
				.findByUtenteUtenteIdAndFineValiditaIsNull(user.getUtenteId());
		
		if (!existingTokens.isEmpty()) {
			log.info("[Shibboleth-SSO] TokenService: Marcatura come scaduti di {} token esistenti", existingTokens.size());
			LocalDateTime now = LocalDateTime.now();
			for (AccrualAuthTToken token : existingTokens) {
				token.setFineValidita(now);
				token.setGestDataDel(now);
				accrualAuthTTokenRepository.saveAndFlush(token);
			}
		}
		
		return generateToken(user, provider, annoBilancio, faseBilancio);
	}

	@Override
	public String findByUtenteUtenteIdAndFineValiditaIsNull(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio) {
		log.info("[Shibboleth-SSO] TokenService: Richiesta token per utente ID: {}, provider: {}, anno: {}, fase: {}",
			user.getUtenteId(), provider, annoBilancio, faseBilancio);

		List<AccrualAuthTToken> listTokenUser = accrualAuthTTokenRepository
				.findByUtenteUtenteIdAndFineValiditaIsNull(user.getUtenteId());

		if (!listTokenUser.isEmpty()) {
			log.info("[Shibboleth-SSO] TokenService: Trovati {} token esistenti per utente", listTokenUser.size());
			List<AccrualAuthTToken> listTokenUserValid = updateExpiredToken(listTokenUser);

			if (listTokenUserValid.isEmpty()) {
				log.info("[Shibboleth-SSO] TokenService: Tutti i token esistenti sono scaduti, genero nuovo token");
				return generateToken(user, provider, annoBilancio, faseBilancio);
			} else {
				log.info("[Shibboleth-SSO] TokenService: Riutilizzo token esistente valido (nonce: {})", 
					CollectionUtils.firstElement(listTokenUserValid).getTokenNonce());
				return CollectionUtils.firstElement(listTokenUserValid).getTokenValue();
			}
		}

		log.info("[Shibboleth-SSO] TokenService: Nessun token esistente, genero nuovo token");
		return generateToken(user, provider, annoBilancio, faseBilancio);
	}

	@Override
	public Optional<AccrualAuthTToken> findByTokenNonce(String tokenNonce) {
		return accrualAuthTTokenRepository.findByTokenNonce(tokenNonce);
	}

	private String generateToken(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio) {
		log.info("[Shibboleth-SSO] TokenService: Generazione nuovo JWT per utente: {}, provider: {}, anno: {}, fase: {}",
			user.getUtenteCodiceUnivoco(), provider, annoBilancio, faseBilancio);
		String jwt = jwtGenerator.generateJwtString(user, provider, annoBilancio, faseBilancio);
		log.info("[Shibboleth-SSO] TokenService: JWT generato, lunghezza: {} caratteri", jwt.length());
		
		Claims claims = jwtUtils.parseClaims(jwt);
		log.info("[Shibboleth-SSO] TokenService: Claims JWT - subject: {}, ente: {}, ruolo: {}", 
			claims.getSubject(), claims.get("ente_codice_univoco"), claims.get("ruolo"));
		
		AccrualAuthTToken accrualAuthTToken = AccrualAuthTToken.builder().tokenValue(jwt)
				.tokenStartTime(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()))
				.tokenNonce(claims.get("nonce").toString()).utente(user).build();

		accrualAuthTTokenRepository.saveAndFlush(accrualAuthTToken);
		log.info("[Shibboleth-SSO] TokenService: JWT salvato nel database - Nonce: {}", claims.get("nonce").toString());
		return jwt;
	}

	public List<AccrualAuthTToken> updateExpiredToken(List<AccrualAuthTToken> tokens) {
        return tokens.stream()
                .filter(token -> {
                	try {
                		jwtUtils.parseClaims(token.getTokenValue());
                	} catch (ExpiredJwtException e) {
                    	log.warn("Token scaduto per utente {}, marchio come invalido", token.getUtente().getUtenteId());
                    	token.setFineValidita(LocalDateTime.now());
                    	token.setGestDataDel(LocalDateTime.now());
                    	accrualAuthTTokenRepository.saveAndFlush(token);
                        return false;
					} catch (JwtException e) {
                    	log.warn("Token non valido per utente {} (firma errata o malformato), marchio come invalido: {}", token.getUtente().getUtenteId(), e.getMessage());
                    	token.setFineValidita(LocalDateTime.now());
                    	token.setGestDataDel(LocalDateTime.now());
                    	accrualAuthTTokenRepository.saveAndFlush(token);
                        return false;
					}
                    return true;
                })
                .collect(Collectors.toList());
    }

	public Optional<AccrualAuthTToken> deleteToken(String token){
		log.debug("Richiesta cancellazione token");
		Claims claims;

	    try {
	        claims = jwtUtils.parseClaims(token);
	    } catch (Exception e) {
	        log.warn("Impossibile parsificare il token: {}", e.getMessage());
	        return Optional.empty();
	    }

	    if (claims == null) {
	        log.warn("Claims nulli per il token");
	        return Optional.empty();
	    }

	    String nonce = claims.get("nonce", String.class);

	    if (nonce == null) {
	        log.warn("Nonce null nei claims del token");
	        return Optional.empty();
	    }

	    log.debug("Eliminazione token con nonce: {}", nonce);
	    return accrualAuthTTokenRepository.findByTokenNonce(nonce)
	            .map(entity -> {
	                LocalDateTime now = LocalDateTime.now();
	                entity.setFineValidita(now);
	                entity.setGestDataUpd(now);
	                entity.setGestDataDel(now);
	                return accrualAuthTTokenRepository.saveAndFlush(entity);
	            });
	}
}
