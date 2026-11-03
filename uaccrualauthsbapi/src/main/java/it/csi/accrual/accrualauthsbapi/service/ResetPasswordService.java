/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service;

import org.springframework.stereotype.Service;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTResetPassword;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;

@Service
public interface ResetPasswordService {
	public AccrualAuthTResetPassword updateResetPassword(AccrualAuthTResetPassword accrualAuthTResetPassword);
	public AccrualAuthTResetPassword findByResetPasswordTokenValue(String resetPasswordTokenValue);
	public AccrualAuthTUtente confirmResetPassword(AccrualAuthTUtente accrualAuthTUtente, String password);
}
