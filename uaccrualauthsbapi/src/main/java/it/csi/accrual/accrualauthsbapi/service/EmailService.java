/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service;

import org.springframework.stereotype.Service;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;

@Service
public interface EmailService {
    public void sendResetPasswordEmail(AccrualAuthTUtente user);
}
