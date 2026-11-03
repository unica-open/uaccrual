/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service;

import org.springframework.stereotype.Service;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;

@Service
public interface AuthService {
	
	public AccrualAuthTUtente login(String email, String enteCodiceUnivoco, String password) throws Exception;
	public AccrualAuthTUtente findByUtenteCodiceUnivoco(String codiceFiscale);
	public AccrualAuthTUtente findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(String codiceFiscale, String enteCodiceUnivoco);

}
