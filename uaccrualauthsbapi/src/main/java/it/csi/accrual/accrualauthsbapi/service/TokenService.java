/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTToken;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;

@Service
public interface TokenService {
	public String findByUtenteUtenteIdAndFineValiditaIsNull(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio);
	public Optional<AccrualAuthTToken> findByTokenNonce(String tokenNonce);
	public List<AccrualAuthTToken> updateExpiredToken(List<AccrualAuthTToken> tokens);
	public Optional<AccrualAuthTToken> deleteToken(String token);
	public String generateOrReplaceToken(AccrualAuthTUtente user, String provider, Integer annoBilancio, String faseBilancio);
}
